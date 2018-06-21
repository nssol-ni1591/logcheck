package logcheck.proj;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;


/*
 * プロジェクトの一覧を取得する
 */
public interface ProjList<E> extends Map<String, E> {

	ProjList<E> load() throws IOException, ClassNotFoundException, SQLException;

	E get(Object projId);

}
