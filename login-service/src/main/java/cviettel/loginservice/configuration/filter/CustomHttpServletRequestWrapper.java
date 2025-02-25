package cviettel.loginservice.configuration.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final String body;

    public CustomHttpServletRequestWrapper(HttpServletRequest request, String body) {
        super(request);
        this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setReadListener(ReadListener readListener) {
                // no-op
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    // Trả về "application/json" cho getContentType()
    @Override
    public String getContentType() {
        return "application/json";
    }

    // Trả về "application/json" cho getHeader("Content-Type")
    @Override
    public String getHeader(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return "application/json";
        }
        return super.getHeader(name);
    }

    // Trả về "application/json" cho getHeaders("Content-Type")
    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return Collections.enumeration(Collections.singletonList("application/json"));
        }
        return super.getHeaders(name);
    }
}
