package logcheck.proj;

import java.util.Map;


/*
 * プロジェクトの一覧を取得する
 */
public interface ProjList<E> extends Map<String, E> {

	ProjList<E> load() throws Exception;

	E get(Object projId);

}
