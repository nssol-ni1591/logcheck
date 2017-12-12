package logcheck.util.weld;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class WeldWrapper<T extends WeldRunner> {

	private static Logger log = Logger.getLogger(WeldWrapper.class.getName());

	private static final String LINE_SEPARATOR = "line.separator";

	private Class<T> cl;

	public WeldWrapper() { }
	public WeldWrapper(Class<T> cl) {
		this.cl = cl;
	}

	public int exec(T application, int argc, String...argv) {
		int rc = 0;
		try {
			if (argv.length < argc) {
				String name = application.getClass().getName();
				int index = name.indexOf('$');
				if (index > 0) {
					name = name.substring(0, index);
				}
				System.err.println(application.usage(name));
				rc = 2;
			}
			else if (!application.check(argc, argv)) {
				rc = 3;
			}
			else if (argc == 2) {
				application.init(argv[0], argv[1]);
			}
			else if (argc == 3) {
				application.init(argv[0], argv[1], argv[2]);
			}
			else {
				System.err.printf("%s: unknown init() parameter%s",
						application.getClass().getName(),
						System.getProperty(LINE_SEPARATOR));
				rc = 4;
			}

			if (rc == 0) {
				rc = application.start(argv, argc);
			}
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, "in app", ex);
			rc = 1;
		}
		return rc;
	}

	public int weld(int argc, String...argv) {
		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			T application = container.select(cl).get();
			rc = exec(application, argc, argv);
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, "in weld", ex);
			rc = -1;
		}
		return rc;
	}
	public <E extends Annotation> int weld(AnnotationLiteral<E> anno, int argc, String...argv) {
		int rc = 0;
		Weld weld = new Weld();
		try (WeldContainer container = weld.initialize()) {
			T application = container.select(cl, anno).get();
			rc = exec(application, argc, argv);
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, "in weld", ex);
			rc = -1;
		}
		return rc;
	}

}
