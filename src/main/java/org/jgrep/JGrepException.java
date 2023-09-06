package org.jgrep;


class JGrepException extends Exception {

    public JGrepException() {
        super();
    }

    public JGrepException(String message) {
        super(message);
    }

    public JGrepException(String message, Throwable cause) {
        super(message, cause);
    }
}
