package logcheck;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import logcheck.fw.FwLogSummary;
import logcheck.known.KnownList;
import logcheck.util.weld.WeldWrapper;

/*
 * FWログの集約処理：
 * FWログを読込、コレクションにログ情報を登録する。出力結果は、TSV形式とし、Excelでの利用を想定している。
 * Checker50との違いは、社外アドレスの分類を行う点 と ISP情報のキャッシュ
 */
public class Checker51 extends Checker50 {

	@Inject private Logger log;

	@Inject private KnownList knownlist51;

	private static final String FILENAME = "isplist.txt";

	@Override
	public void init(String...argv) throws IOException, ClassNotFoundException, SQLException {
		super.knownlist = this.knownlist51;
		File file = new File(FILENAME);
		if (file.exists()) {
			this.knownlist.load(FILENAME);
		}
		else {
			this.knownlist.load(argv[0]);
		}
		this.maglist.load(argv[1]);
		this.sdclist.load(argv[2]);
	}

	@Override
	public void report(final PrintWriter out, final Set<FwLogSummary> list) {
		super.report(out, list);

		try {
			File file = new File(FILENAME);
			knownlist.store(FILENAME);
			log.log(Level.INFO, "knownlist.store: path={0}", file.getAbsolutePath());
		}
		catch (IOException ex) {
			log.log(Level.WARNING, ex.getMessage());
		}
	}

	public static void main(String... argv) {
		int rc = new WeldWrapper(Checker51.class).weld(3, argv);
		System.exit(rc);
	}

}
