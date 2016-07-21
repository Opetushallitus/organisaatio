package fi.vm.sade.organisaatio.service.filters;

//import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;

// Pass on the received ID Chain intact. Load balancer will update this ID chain automatically. Add the same "Caller-Id"
// (clientSubSystemCode) to every sent REST message.
//public class OutputFilter implements ResponseHandler {
//    private static final Logger LOG = LoggerFactory.getLogger(OutputFilter.class);
//
//    @Override
//    public Response handleResponse(Message outMessage, OperationResourceInfo operationResourceInfo, Response response) {
//        String IDChain = (String)outMessage.getExchange().get("ID");
//        // Add callerid and ID chain headers to the output message.
//        if(response != null) {
//            ((ResponseImpl)response).getHeaders().add("ID", IDChain);
//            ((ResponseImpl)response).getHeaders().add("clientSubSystemCode", IDContextMessageHelper.getClientSubSystemCode());
//        }
//        return response;
//    }
//}
