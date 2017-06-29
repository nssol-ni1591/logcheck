package logcheck.site;

/*
 * VPNクライアント証明書が発行されているユーザの一覧を取得する
 */
public interface SiteList {

	SiteList load(String file) throws Exception;

	SiteListIsp get(Object siteId);

}
