import java.io.InputStream;
import java.io.StringWriter;

public class StreamReader extends Thread {
    private StringWriter L;
    private InputStream g;

    public String getResult() {
        return this.L.toString();
    }

    public StreamReader(InputStream is) {
        L = new StringWriter();
        g = is;
    }

    public void run() {
        try {
            int a;
            while((a = this.g.read()) != -1) {
                this.L.write(a);
            }

        } catch (Exception var2) {

        }
    }
}
