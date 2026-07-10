package org.adex.proxy;

import org.adex.backend.Backend;

public interface RequestForwarder {

    Object send(Backend backend, Object req);

}
