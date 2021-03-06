package logcheck.util;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.util.AnnotationLiteral;

public class WeldWrapper {

	private static Logger log = Logger.getLogger(WeldWrapper.class.getName());

	private Class<? extends WeldRunner> cl;

	public WeldWrapper() { }
	public WeldWrapper(Class<? extends WeldRunner> cl) {
		this.cl = cl;
	}

	public int exec(WeldRunner application, int argc, String...argv) {
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
		return weld(null, argc, argv);
	}
	public <E extends Annotation> int weld(AnnotationLiteral<E> anno, int argc, String...argv) {
		int rc = 0;

		try(SeContainer container = SeContainerInitializer.newInstance().initialize()) {
	        // start the container, retrieve a bean and do work with it
			WeldRunner application;
			if (anno == null) {
				// AnnotationがついていないCheckerクラスの生成
				application = container.select(cl).get();
			}
			else {
				// Annotation付きCheckerクラスの生成
				application = container.select(cl, anno).get();
			}
			rc = exec(application, argc, argv);
	    }
		catch (Exception ex) {
			log.log(Level.SEVERE, "in weld", ex);
			rc = -1;
		}
		return rc;
	}

}
