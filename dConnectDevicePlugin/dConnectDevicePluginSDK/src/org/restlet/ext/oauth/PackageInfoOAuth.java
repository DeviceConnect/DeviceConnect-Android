package org.restlet.ext.oauth;

/**
 * OAuthアクセストークンを発行する相手を識別するデータ.<br>
 * - アプリ(Android)の場合は、パッケージ名を入れる。<br>
 * - アプリ(Web)の場合は、パッケージ名にURLを入れる。<br>
 * - デバイスプラグインの場合は、パッケージ名とサービスIDを入れる。<br>
 */
public class PackageInfoOAuth {
	
	/** パッケージ名. */
	private String mPackageName;
	
	/** サービスID(アプリの場合はnullを設定する). */
	private String mServiceId;
	
	/**
	 * コンストラクタ(アプリを指定する場合).
	 * @param packageName	パッケージ名.
	 */
	public PackageInfoOAuth(final String packageName) {
		mPackageName = packageName;
		mServiceId = null;
	}
	
	/**
	 * コンストラクタ(デバイスプラグインを指定する場合).
	 * @param packageName	パッケージ名.
	 * @param serviceId		サービスID.
	 */
	public PackageInfoOAuth(final String packageName, final String serviceId) {
		mPackageName = packageName;
		mServiceId = serviceId;
	}
	
	/**
	 * パッケージ名取得.
	 * @return	パッケージ名
	 */
	public String getPackageName() {
		return mPackageName;
	}
	
	/**
	 * サービスID取得.
	 * @return	サービスID
	 */
	public String getServiceId() {
		return mServiceId;
	}
	
	/**
	 * オブジェクト比較.
	 * @param o	比較対象のオブジェクト
	 * @return true: 同じ値を持つオブジェクトである。 / false: 異なる値を持っている。
	 */
	@Override
	public boolean equals(final Object o) {
		
		PackageInfoOAuth cmp1 = this;
		PackageInfoOAuth cmp2 = (PackageInfoOAuth) o;
		
		boolean isEqualPackageName = false;
		if (cmp1.getPackageName() == null && cmp2.getPackageName() == null) {		/* 両方null */
			isEqualPackageName = true;
		} else if (cmp1.getPackageName() != null && cmp2.getPackageName() != null 	/* 両方同じ文字列 */
				&& cmp1.getPackageName().equals(cmp2.getPackageName())) {
			isEqualPackageName = true;
		}
		
		boolean isEqualServiceId = false;
		if (cmp1.getServiceId() == null && cmp2.getServiceId() == null) {				/* 両方null */
			isEqualServiceId = true;
		} else if (cmp1.getServiceId() != null && cmp2.getServiceId() != null 		/* 両方同じ文字列 */
				&& cmp1.getServiceId().equals(cmp2.getServiceId())) {
			isEqualServiceId = true;
		}
		
		if (isEqualPackageName && isEqualServiceId) {
			return true;
		}
		return false;
	}
	
	/**
	 * ハッシュ値を返す.
	 * @return	ハッシュ値 
	 */
	@Override
	public int hashCode() {
		
		String str = "";
		if (getPackageName() != null) {
			str += getPackageName();
		} else {
			str += "(null)";
		}
		if (getServiceId() != null) {
			str += getServiceId();
		} else {
			str += "(null)";
		}
		
		int hashCode = str.hashCode();
		return hashCode;
	}
}
