package com.brettonw;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletBase {
    private static final Logger log = LogManager.getLogger (BagTestServer.class);

    @Override
    public void init (ServletConfig config) throws ServletException {
        super.init (config);
    }

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BagObject query = parseQuery (request);
        doRequest (query, null, request, response);
    }

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BagObject query = parseQuery (request);

        // extract the parameters, they must be a bag object
        BagObject parameters = BagObjectFrom.inputStream (request.getInputStream ());

        doRequest (query, parameters, request, response);
    }

    private BagObject parseQuery (HttpServletRequest request) {
        BagObject query = new BagObject ();
        String queryString = request.getQueryString ();
        if (queryString != null) {
            String[] queryParameters = queryString.split ("&");
            for (String queryParameter : queryParameters) {
                String[] pair = queryParameter.split ("=");
                query.put (pair[0], pair[1]);
            }
        }
        return query;
    }

    private void doRequest (BagObject query, BagObject parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // set the response to be JSON
        response.setContentType ("application/json;charset=UTF-8");
        response.setCharacterEncoding ("UTF-8");

        String command = query.getString ("command", () -> "echo");
        String responseString = new BagObject ().put ("command", command).toString ();
        log.info ("Command: " + command);
        switch (command) {
            case "echo":
                if (parameters != null) {
                    responseString = parameters.toString ();
                } else {
                    responseString = query.toString ();
                }
                break;
            case "ip":
                responseString = new BagObject ().put ("ip", request.getRemoteAddr ()).toString ();
                break;
            case "data":
                if (parameters != null) {
                    Bag data = parameters.getBagArray ("data");
                    if (data == null) {
                        data = parameters.getBagObject ("data");
                        if (data != null) {
                            data = new BagArray ().add (data);
                        }
                    }
                    if (data != null) {
                        responseString = data.toString ();
                    } else {
                        responseString = new BagArray ().add (new BagObject ().put ("error", "'data' is not a valid object or array")).toString ();
                    }
                } else {
                    responseString = new BagArray ().add (new BagObject ().put ("error", "invalid parameters")).toString ();
                }
                break;
        }

        PrintWriter out = response.getWriter ();
        out.println (responseString);
        //out.flush ();
        out.close ();
    }
}
