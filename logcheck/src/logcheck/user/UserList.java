package logcheck.user;

import java.util.Map;

import logcheck.isp.IspList;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface UserList<E extends IspList> extends Map<String, UserListBean<E>> {

	UserList<E> load(Class<E> clazz) throws Exception;

	UserList<E> load(String sql, Class<E> clazz) throws Exception;

//	UserListBean<E> get(String userId);

}
