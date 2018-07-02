package logcheck.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import logcheck.site.SiteList;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface UserList<E> extends Map<String, E> {

	UserList<E> load(String sql, SiteList sitelist) throws IOException, ClassNotFoundException, SQLException;

	E get(Object userId);

}
