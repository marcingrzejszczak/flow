package com.vaadin.flow.server;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.server.MockServletServiceSessionSetup.TestVaadinServletService;
import com.vaadin.flow.theme.AbstractTheme;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test class for testing es6 resolution by browser capability. This is valid
 * only for bower mode where we need to decide ourselves.
 */
public class VaadinServletServiceTest {

    private final class TestTheme implements AbstractTheme {
        @Override
        public String getBaseUrl() {
            return "/raw/";
        }

        @Override
        public String getThemeUrl() {
            return "/theme/";
        }
    }

    private MockServletServiceSessionSetup mocks;
    private TestVaadinServletService service;
    private VaadinServlet servlet;

    @Before
    public void setup() throws Exception {
        mocks = new MockServletServiceSessionSetup();
        service = mocks.getService();

        servlet = mocks.getServlet();
    }

    @After
    public void tearDown() {
        mocks.cleanup();
    }

    @Test
    public void resolveNullThrows() {
        try {
            service.resolveResource(null);
            Assert.fail("null should not resolve");
        } catch (NullPointerException e) {
            Assert.assertEquals("Url cannot be null", e.getMessage());
        }
    }

    @Test
    public void resolveResource() {
        Assert.assertEquals("", service.resolveResource(""));
        Assert.assertEquals("foo", service.resolveResource("foo"));
        Assert.assertEquals("/foo", service.resolveResource("context://foo"));
    }

    @Test
    public void resolveResourceNPM_production() {
        mocks.setProductionMode(true);

        Assert.assertEquals("", service.resolveResource(""));
        Assert.assertEquals("foo", service.resolveResource("foo"));
        Assert.assertEquals("/foo", service.resolveResource("context://foo"));
    }

    @Test
    public void getContextRootRelativePath_useVariousContextPathAndServletPathsAndPathInfo()
            throws Exception {
        String location;

        /* SERVLETS */
        // http://dummy.host:8080/contextpath/servlet
        // should return . (relative url resolving to /contextpath)
        location = testLocation("http://dummy.host:8080", "/contextpath",
                "/servlet", "");
        Assert.assertEquals("./../", location);

        // http://dummy.host:8080/contextpath/servlet/
        // should return ./.. (relative url resolving to /contextpath)
        location = testLocation("http://dummy.host:8080", "/contextpath",
                "/servlet", "/");
        Assert.assertEquals("./../", location);

        // http://dummy.host:8080/servlet
        // should return "."
        location = testLocation("http://dummy.host:8080", "", "/servlet", "");
        Assert.assertEquals("./../", location);

        // http://dummy.host/contextpath/servlet/extra/stuff
        // should return ./../.. (relative url resolving to /contextpath)
        location = testLocation("http://dummy.host", "/contextpath", "/servlet",
                "/extra/stuff");
        Assert.assertEquals("./../", location);

        // http://dummy.host/context/path/servlet/extra/stuff
        // should return ./../.. (relative url resolving to /context/path)
        location = testLocation("http://dummy.host", "/context/path",
                "/servlet", "/extra/stuff");
        Assert.assertEquals("./../", location);

    }

    @Test
    public void init_classLoaderIsSetUsingServletContext()
            throws ServiceException {
        VaadinServlet servlet = Mockito.mock(VaadinServlet.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(servlet.getServletContext()).thenReturn(context);

        ClassLoader loader = Mockito.mock(ClassLoader.class);
        Mockito.when(context.getClassLoader()).thenReturn(loader);

        VaadinServletService service = new VaadinServletService(servlet,
                mocks.getDeploymentConfiguration()) {
            @Override
            protected Instantiator createInstantiator()
                    throws ServiceException {
                return Mockito.mock(Instantiator.class);
            }

            @Override
            protected List<RequestHandler> createRequestHandlers()
                    throws ServiceException {
                return Collections.emptyList();
            }
        };

        service.init();

        Assert.assertSame(loader, service.getClassLoader());
    }

    @Test
    public void getPwaRegistry_servletInitialized_getsRegistry() {
        MockServletServiceSessionSetup.TestVaadinServlet vaadinServlet = Mockito
                .spy(mocks.getServlet());
        // Restore original behavior of getServletContext
        Mockito.when(vaadinServlet.getServletContext()).thenAnswer(
                i -> vaadinServlet.getServletConfig().getServletContext());
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                mocks.getDeploymentConfiguration());
        Assert.assertNotNull(service.getPwaRegistry());
    }

    @Test
    public void getPwaRegistry_servletNotInitialized_getsNull() {
        MockServletServiceSessionSetup.TestVaadinServlet vaadinServlet = Mockito
                .spy(mocks.getServlet());
        // Restore original behavior of getServletContext
        Mockito.when(vaadinServlet.getServletContext()).thenAnswer(
                i -> vaadinServlet.getServletConfig().getServletContext());
        VaadinServletService service = new VaadinServletService(vaadinServlet,
                mocks.getDeploymentConfiguration());
        vaadinServlet.destroy();
        Assert.assertNull(service.getPwaRegistry());
    }

    private String testLocation(String base, String contextPath,
            String servletPath, String pathInfo) throws Exception {

        HttpServletRequest request = createNonIncludeRequest(base, contextPath,
                servletPath, pathInfo);

        VaadinServletService service = Mockito.mock(VaadinServletService.class);
        Mockito.doCallRealMethod().when(service)
                .getContextRootRelativePath(Mockito.any());
        String location = service.getContextRootRelativePath(
                servlet.createVaadinRequest(request));
        return location;
    }

    private HttpServletRequest createNonIncludeRequest(String base,
            String realContextPath, String realServletPath, String pathInfo)
            throws Exception {
        HttpServletRequest request = createRequest(base, realContextPath,
                realServletPath, pathInfo);
        Mockito.when(
                request.getAttribute("jakarta.servlet.include.context_path"))
                .thenReturn(null);
        Mockito.when(
                request.getAttribute("jakarta.servlet.include.servlet_path"))
                .thenReturn(null);

        return request;
    }

    /**
     * Creates a HttpServletRequest mock using the supplied parameters.
     *
     * @param base
     *            The base url, e.g. http://localhost:8080
     * @param contextPath
     *            The context path where the application is deployed, e.g.
     *            /mycontext
     * @param servletPath
     *            The servlet path to the servlet we are testing, e.g. /myapp
     * @param pathInfo
     *            Any text following the servlet path in the request, not
     *            including query parameters, e.g. /UIDL/
     * @return A mock HttpServletRequest object useful for testing
     * @throws MalformedURLException
     */
    private HttpServletRequest createRequest(String base, String contextPath,
            String servletPath, String pathInfo) throws MalformedURLException {
        URL url = new URL(base + contextPath + pathInfo);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("null.lock", new ReentrantLock()); // for session
        attributes.put("requestStartTime", System.currentTimeMillis()); // for request end
        Mockito.when(request.isSecure())
                .thenReturn(url.getProtocol().equalsIgnoreCase("https"));
        Mockito.when(request.getServerName()).thenReturn(url.getHost());
        Mockito.when(request.getServerPort()).thenReturn(url.getPort());
        Mockito.when(request.getRequestURI()).thenReturn(url.getPath());
        Mockito.when(request.getContextPath()).thenReturn(contextPath);
        Mockito.when(request.getPathInfo()).thenReturn(pathInfo);
        Mockito.when(request.getServletPath()).thenReturn(servletPath);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(request.getSession()).thenReturn(session);
        Mockito.when(request.getSession(Mockito.anyBoolean())).thenReturn(session);
        stubSessionAttributes(session, attributes);
        stubAttributes(request, attributes);
        return request;
    }

    private static void stubSessionAttributes(HttpSession session,
            Map<String, Object> attributes) {
        Mockito.when(
                session.getAttribute(Mockito.anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> attributes.put(invocation.getArgument(0), invocation.getArgument(1))).when(
                session).setAttribute(Mockito.anyString(), Mockito.anyString());
    }

    private static void stubAttributes(HttpServletRequest request,
            Map<String, Object> attributes) {
        Mockito.when(
                request.getAttribute(Mockito.anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> attributes.put(invocation.getArgument(0), invocation.getArgument(1))).when(
                request).setAttribute(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void filtersAreCalledWhenHandlingARequest()
            throws MalformedURLException {
        VaadinRequest request = servlet.createVaadinRequest(createRequest("http://dummy.host:8080/", "/contextpath", "/servlet", "/"));
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        service.setVaadinFilters(Collections.singletonList(new MyFilter()));
        service.getRequestHandlers().clear();
        service.getRequestHandlers().add(new ExceptionThrowingRequestHandler());

        try {
            service.handleRequest(request, response);
        } catch (ServiceException ex) {
            Assert.assertTrue("The exception was the one coming from RequestHandler", ex.getMessage().contains("BOOM!"));
        }

        Assert.assertEquals("Filter was called on request start", "true", request.getAttribute("started"));
        Assert.assertEquals("Filter was called on exception handling", "true", request.getAttribute("exception handled"));
        Assert.assertEquals("Filter was called in the finally block", "true", request.getAttribute("ended"));
    }

    static class ExceptionThrowingRequestHandler implements RequestHandler {

        @Override
        public boolean handleRequest(VaadinSession session,
                VaadinRequest request, VaadinResponse response)
                throws IOException {
            throw new IllegalStateException("BOOM!");
        }
    }

    static class MyFilter implements VaadinFilter {

        @Override
        public void requestStart(VaadinRequest request,
                VaadinResponse response) {
            request.setAttribute("started", "true");
            // An exception thrown here will not be caught by other methods of the filter!
        }

        @Override
        public void handleException(VaadinRequest request,
                VaadinResponse response, VaadinSession vaadinSession,
                Exception t) {
            if (t instanceof IllegalStateException ex) {
                Assert.assertEquals("BOOM!", ex.getMessage());
                request.setAttribute("exception handled", "true");
                return;
            }
            throw new AssertionError("Invalid exception thrown. Wanted <IllegalStateException> got <" + t.getClass() + ">", t);
        }

        @Override
        public void requestEnd(VaadinRequest request, VaadinResponse response,
                VaadinSession session) {
            request.setAttribute("ended", "true");
        }
    }
}
