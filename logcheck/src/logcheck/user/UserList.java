package logcheck.user;

import java.util.Map;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface UserList<E> extends Map<String, E> {

	UserList<E> load() throws Exception;

	UserList<E> load(String sql) throws Exception;

//	E get(String userId);

}
