package logcheck.user;

import java.util.Map;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface UserList extends Map<String, UserListBean> {

	UserList load() throws Exception;

	UserList load(String sql) throws Exception;

//	UserListBean<E> get(String userId);

}
