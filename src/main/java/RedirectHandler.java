import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;

public class RedirectHandler implements HttpHandler {

    private final Extension extension;

    public RedirectHandler(Extension extension) {
        this.extension = extension;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {
        if (!this.extension.isActive) {
            return null;
        }
        Boolean toolTypeEnabled = extension.isToolTypeEnabled.get(httpRequestToBeSent.toolSource().toolType());
        if (toolTypeEnabled == null) {
            // Source of the request is not a supported type (Proxy, Repeater, etc.)
            return null;
        }
        if (!toolTypeEnabled) {
            // Source of the request is not enabled for redirection (Proxy, Repeater, etc.)
            return null;
        }
        // At this stage, source of the redirection is supported, and enabled
        // Check if the host of the request is in the list of hosts to be redirected
        HttpService srcHttpService = httpRequestToBeSent.httpService();
        int indexOfHost = extension.srcHosts.indexOf(srcHttpService.host());
        if (indexOfHost < 0) {
            // host is not in the list of hosts to redirect
            return null;
        }
        // Host needs to be redirected
        String dstHost = extension.dstHosts.get(indexOfHost);
        HttpService dstHttpService = HttpService.httpService(dstHost, srcHttpService.port(), srcHttpService.secure());
        HttpRequest newReq = httpRequestToBeSent.withService(dstHttpService);
        if (extension.updateHostHeader) {
            String hostHeader = newReq.headerValue("Host");
            hostHeader = hostHeader.replace(extension.srcHosts.get(indexOfHost), dstHost);
            newReq = newReq.withHeader("Host", hostHeader);
        }
        return RequestToBeSentAction.continueWith(newReq);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return null;
    }
}
