import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.regex.Pattern;

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
        // At this stage, source tool of the request is supported, and enabled
        // Check if the host of the request is in the list of hosts to be redirected
        HttpService srcHttpService = httpRequestToBeSent.httpService();
        String reqHost = srcHttpService.host();
        int indexOfHost = extension.srcHosts.indexOf(reqHost);
        if (indexOfHost < 0) {
            // host is not in the list of hosts to redirect
            return null;
        }

        // Host match found. Now find match for path
        // Since host list is usually small, simple for loop is better for performance
        // If the host list grows over 20, this may have a performance impact
        int matchIndex = -1;
        do {
            Pattern pathPattern = extension.srcPath.get(indexOfHost);
            if (pathPattern == null) {
                // request should be redirected
                matchIndex = indexOfHost;
                break;
            }
            if (pathPattern.matcher(httpRequestToBeSent.path()).matches()) {
                // request should be redirected
                matchIndex = indexOfHost;
                break;
            }
            for ( indexOfHost = indexOfHost + 1;
                  indexOfHost < extension.tableSize && !reqHost.equals(extension.srcHosts.get(indexOfHost));
                  indexOfHost++) {;}
        } while (indexOfHost < extension.tableSize);

        if (matchIndex == -1) {
            // path match could not be found
            return null;
        }

        // Hostname and path matched
        // Host should be redirected
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
