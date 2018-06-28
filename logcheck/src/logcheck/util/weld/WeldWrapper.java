package logcheck.util.weld;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.util.AnnotationLiteral;

public class WeldWrapper<T extends WeldRunner> {

	private static Logger log = Logger.getLogger(WeldWrapper.class.getName());

	private Class<T> cl;

	public WeldWrapper() { }
	public WeldWrapper(Class<T> cl) {
		this.cl = cl;
	}

	public int exec(T application, int argc, String...argv) {
		return exec(null, application, argc, argv);
	}
	public int exec(PrintWriter out, T application, int argc, String...argv) {
		int rc = 0;
		try {
			if (argv.length < argc) {
				String name = application.getClass().getName();
				int index = name.indexOf('$');
				if (index > 0) {
					name = name.substring(0, index);
				}
				//Invoke method(s) only conditionally.
				String msg = application.usage(name);
				log.log(Level.SEVERE, msg);
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
				log.log(Level.SEVERE, "{0}: unknown init() parameter size (argc={1})",
						new Object[] { application.getClass().getName(), argc });
				rc = 4;
			}

			if (rc == 0) {
				if (out == null) {
					try (PrintWriter out2 = new PrintWriter(System.out)) {
						rc = application.start(out2, argv, argc);
		        	}
		        }
				else {
					rc = application.start(out, argv, argc);
				}
			}
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, "in app", ex);
			rc = 1;
		}
		return rc;
	}

	public int weld(int argc, String...argv) {
		return weld(null, null, argc, argv);
	}
	public int weld(PrintWriter out, int argc, String...argv) {
		return weld(out, null, argc, argv);
	}

	public <E extends Annotation> int weld(AnnotationLiteral<E> anno, int argc, String...argv) {
		return weld(null, anno, argc, argv);
	}
	public <E extends Annotation> int weld(PrintWriter out, AnnotationLiteral<E> anno, int argc, String...argv) {
		int rc = 0;

		try(SeContainer container = SeContainerInitializer.newInstance().initialize()) {
	        // start the container, retrieve a bean and do work with it
			T application;
			if (anno == null) {
				// AnnotationがついていないCheckerクラスの生成
				application = container.select(cl).get();
			}
			else {
				// Annotation付きCheckerクラスの生成
				application = container.select(cl, anno).get();
			}
			rc = exec(out, application, argc, argv);
	    }
		catch (Exception ex) {
			log.log(Level.SEVERE, "in weld", ex);
			rc = -1;
		}
		return rc;
	}

}
