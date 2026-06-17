package com.bajaj.bfhl.service;

import com.bajaj.bfhl.dto.BfhlRequest;
import com.bajaj.bfhl.dto.BfhlResponse;

public interface BfhlService {

    /**
     * Processes the incoming BFHL request and returns categorized data.
     *
     * @param request   the request payload containing a mixed data array
     * @param requestId the X-Request-Id header value
     * @return a fully populated BfhlResponse
     */
    BfhlResponse process(BfhlRequest request, String requestId);
}
