package fi.vm.sade.organisaatio.integrationtest;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Antti
 */
public final class JettyTstUtils {

    private static ApplicationContext previouslyCreatedAppCtx;

    public static Server startJettyWithCxf(int port, String... springAppCtx) throws Exception {
        // create jetty server
        Server server = new Server(port);

        // create webapp context and register it to server
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        // create spring application context for webapp
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(springAppCtx);

        // create cxf servlet
        CXFNonSpringServlet servlet = new CXFNonSpringServlet();
        servlet.setBus((Bus) applicationContext.getBean("cxf"));
        ServletHandler servletHandler = new ServletHandler();
        ServletHolder servletHolder = new ServletHolder(servlet);
        servletHolder.setName("cxfServlet");
        servletHandler.addServlet(servletHolder);

        // create mapping for cxf servlet
        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName("cxfServlet");
        servletMapping.setPathSpec("/cxf/*");
        servletHandler.setServletMappings(new ServletMapping[]{servletMapping});
        context.addHandler(servletHandler);

        // start jetty
        server.start();
        previouslyCreatedAppCtx = applicationContext;
        return server;
    }

    public static void stop(Server server) throws Exception {
        if (server != null) {
            server.stop();
            server.join();
        }
        Thread.sleep(3000); // TODO: jos toinen testi sisältäen jetyn käynnistyy heti perään, se failaa kummiin yhteysvirheisiin jollei tässä hieman odoteta
    }

    public static ApplicationContext getPreviouslyCreatedAppCtx() {
        return previouslyCreatedAppCtx;
    }
}
