package logcheck.util;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import logcheck.annotations.WithElaps;

@Interceptor // インターセプターの宣言
@Dependent
@WithElaps // バインド用アノテーション
@Priority(Interceptor.Priority.APPLICATION) // 優先度
public class WithElapsInterceptor {
	/**
	 * インターセプターのメソッド
	 * 
	 * @param ic 実行コンテキスト - 本来実行される処理。
	 * @return 本来実行される処理の戻り値
	 * @throws Exception 何らかの例外
	 */
	@AroundInvoke
	public Object invoke(InvocationContext ic) throws Exception {
		// ターゲットは、CDIのクライアントプロキシなので、スーパークラスを取得。
		String classAndMethod = ic.getTarget().getClass().getSuperclass().getName()
				+ "#" + ic.getMethod().getName();

		// メソッド開始前のログ
		System.err.println("start " + classAndMethod + ".");

		long time;
		Object rc = null;
		try {
			// メソッドの実行
			time = System.currentTimeMillis();
			rc = ic.proceed();
			time = System.currentTimeMillis() - time;
		}
		catch (Exception ex) {
			// 例外のログを出したら、例外はそのまま再スローする。
			// トランザクションインターセプターの内部で処理されるので、
			// ここでは根本例外が出る。
			System.err.println(ex);
			throw ex;
		}

		// メソッド終了後のログ
		System.err.println("end " + classAndMethod + ". (elaps=" + time + " ms)");
		return rc;
	}
}
