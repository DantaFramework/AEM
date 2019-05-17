package danta.aem.util;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.IOException;
import java.io.InputStream;

public class LazyInputStream extends InputStream {

    /** The JCR Value from which the input stream is requested on demand */
    private final Value value;

    /** The inputstream created on demand, null if not used */
    private InputStream delegatee;

    public LazyInputStream(Value value) {
        this.value = value;
    }

    /**
     * Closes the input stream if acquired otherwise does nothing.
     */
    @Override
    public void close() throws IOException {
        if (delegatee != null) {
            delegatee.close();
        }
    }

    @Override
    public int available() throws IOException {
        return getStream().available();
    }

    @Override
    public int read() throws IOException {
        return getStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getStream().read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return getStream().skip(n);
    }

    @Override
    public boolean markSupported() {
        try {
            return getStream().markSupported();
        } catch (IOException ioe) {
            // ignore
        }
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            getStream().mark(readlimit);
        } catch (IOException ioe) {
            // ignore
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        getStream().reset();
    }

    /** Actually retrieves the input stream from the underlying JCR Value */
    private InputStream getStream() throws IOException {
        if (delegatee == null) {
            try {
                delegatee = value.getBinary().getStream();
            } catch (RepositoryException re) {
                throw (IOException) new IOException(re.getMessage()).initCause(re);
            }
        }
        return delegatee;
    }
}
