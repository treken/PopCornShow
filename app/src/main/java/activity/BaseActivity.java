package activity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import br.com.icaro.filme.BuildConfig;
import br.com.icaro.filme.R;
import domain.Api;
import domain.busca.MultiSearch;
import oscar.OscarActivity;
import pessoaspopulares.PersonPopularActivity;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import utils.Constantes;
import utils.UtilsApp;


/**
 * Created by icaro on 24/06/16.
 */
@SuppressLint("Registered")
@Keep
public class BaseActivity extends AppCompatActivity {

	protected DrawerLayout drawerLayout;
	protected NavigationView navigationView;
	private ImageView imgUserPhoto;
	private TextView tUserName;
	private TextView tLogin;
	private TextView textLogin;
	private FirebaseRemoteConfig mFirebaseRemoteConfig;
	private FirebaseUser user;
	private Dialog dialog;
	private CompositeSubscription subscriptions = new CompositeSubscription();

	public static void SnackBar(final View view, String msg) {

		Snackbar.make(view, msg
				, Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
			@Override
			public void onShown(Snackbar snackbar) {
				super.onShown(snackbar);
				view.setAlpha(0);
			}

			@Override
			public void onDismissed(Snackbar snackbar, int event) {
				super.onDismissed(snackbar, event);
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user != null) {
					view.setAlpha(1);
				}
			}
		}).show();
	}

	static public String getLocale() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return Locale.getDefault().toLanguageTag();
		} else {
			return Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
		}
	}


	@SuppressWarnings("deprecation")
	protected void setUpToolBar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitleTextColor(getResources().getColor(R.color.white));
			setSupportActionBar(toolbar);
		}
	}

	protected void setupNavDrawer() {

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
		actionBar.setDisplayHomeAsUpEnabled(true);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layoyt);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setItemIconTintList(null);


		if (navigationView != null && drawerLayout != null)

		{

			View view = getLayoutInflater().inflate(R.layout.nav_drawer_header, navigationView);
			view.setVisibility(View.VISIBLE);
			view.findViewById(R.id.textLogin);
			imgUserPhoto = (ImageView) view.findViewById(R.id.imgUserPhoto);

			tUserName = (TextView) view.findViewById(R.id.tUserName);
			tLogin = (TextView) view.findViewById(R.id.tLogin);
			textLogin = (TextView) view.findViewById(R.id.textLogin);


			navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem item) {
					//Seleciona a Linha
					item.setChecked(true);
					//Fecha o menu
					drawerLayout.closeDrawers();
					//trata o evento do menu
					onNavDrawerItemSelected(item);
					return true;
				}
			});
		}

		validarNavDrawerComLogin();
	}

	private void validarNavDrawerComLogin() {

		FirebaseAuth auth = FirebaseAuth.getInstance();
		Menu grupo_login = navigationView.getMenu();


		if (auth.getCurrentUser() == null) {
			textLogin.setText(R.string.fazer_login);
			textLogin.setTextSize(20);
			textLogin.setVisibility(View.VISIBLE);
			imgUserPhoto.setImageResource(R.drawable.add_user);
			grupo_login = navigationView.getMenu();
			grupo_login.removeGroup(R.id.menu_drav_logado);
			imgUserPhoto.setOnClickListener(onClickListenerLogar());

		} else {
			user = auth.getCurrentUser();

			if (user.isAnonymous()) {
				textLogin.setVisibility(View.VISIBLE);
				grupo_login.setGroupVisible(R.id.menu_drav_logado, true);
				tLogin.setText(R.string.anonymous);
				tUserName.setText(R.string.criar_login_popcorn);
				imgUserPhoto.setImageResource(R.drawable.add_user);
				imgUserPhoto.setOnClickListener(onClickListenerlogado());
			} else {
				if (user.getProviders() != null)
					// Log.d(TAG, user.getProviders().get(0));
					switch (user.getProviders().get(0)) {

						case "google.com": {
							textLogin.setVisibility(View.VISIBLE);
							grupo_login.setGroupVisible(R.id.menu_drav_logado, true);
							tUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
							tLogin.setText(user.getEmail() != null ? user.getEmail() : "");
							Picasso.get().load(user.getPhotoUrl())
									.placeholder(R.drawable.person)
									.into(imgUserPhoto);
							imgUserPhoto.setOnClickListener(onClickListenerlogado());
							break;
						}

						case "facebook.com": {
							textLogin.setVisibility(View.VISIBLE);
							grupo_login.setGroupVisible(R.id.menu_drav_logado, true);
							tUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
							tLogin.setText(user.getEmail() != null ? user.getEmail() : "");
							Picasso.get().load(user.getPhotoUrl())
									.placeholder(R.drawable.person)
									.into(imgUserPhoto);
							imgUserPhoto.setOnClickListener(onClickListenerlogado());
							break;
						}

						case "password": {
							textLogin.setVisibility(View.GONE);
							grupo_login.setGroupVisible(R.id.menu_drav_logado, true);
							tUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
							tLogin.setText(user.getEmail() != null ? user.getEmail() : "");
							Picasso.get().load(user.getPhotoUrl())
									.placeholder(R.drawable.person)
									.into(imgUserPhoto);
							imgUserPhoto.setOnClickListener(onClickListenerlogado());
							break;
						}
					}
			}
		}
	}


	protected void setCheckable(int id) {

		switch (id) {

			case R.id.menu_drav_home: {
				this.navigationView.setCheckedItem(id);
			}
			case R.id.menu_drav_person: {
				this.navigationView.setCheckedItem(id);
			}
			case R.id.menu_drav_oscar: {
				this.navigationView.setCheckedItem(id);
			}
			case R.id.seguindo: {
				this.navigationView.setCheckedItem(id);

			}
//            case R.id.list: {
//                this.navigationView.setCheckedItem(id);
//            } //Metoda da Api não carrega filmes_main da list.

		}//??????????? Cade os outros?

	}

	private void onNavDrawerItemSelected(MenuItem menuItem) {
		Intent intent;
		switch (menuItem.getItemId()) {

			case R.id.menu_drav_home:

				intent = new Intent(this, MainActivity.class);
				intent.putExtra(Constantes.INSTANCE.getABA(), R.id.menu_drav_home);
				startActivity(intent);
				break;

			case R.id.nav_item_settings:

				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.favorite:

				intent = new Intent(this, FavoriteActivity.class);
				startActivity(intent);

				break;
			case R.id.rated:

				intent = new Intent(this, RatedActivity.class);
				startActivity(intent);
				break;
			case R.id.watchlist:

				intent = new Intent(this, WatchListActivity.class);
				startActivity(intent);
				break;

			case R.id.menu_drav_person:
				intent = new Intent(this, PersonPopularActivity.class);
				startActivity(intent);
				break;

			case R.id.menu_drav_oscar:

				intent = new Intent(this, OscarActivity.class);
				intent.putExtra(Constantes.INSTANCE.getLISTA_ID(), getResources().getString(R.string.id_oscar));
				intent.putExtra(Constantes.INSTANCE.getLISTA_NOME(), R.string.oscar);
				startActivity(intent);
				break;

			case R.id.menu_drav_surpresa:
				getParametrosDoRemoteConfig();
				break;

			case R.id.seguindo:

				intent = new Intent(this, SeguindoActivity.class);
				intent.putExtra(Constantes.INSTANCE.getLISTA_ID(), "28");
				intent.putExtra(Constantes.INSTANCE.getLISTA_NOME(), R.string.oscar);
				startActivity(intent);
				break;

		}
	}

	private void getParametrosDoRemoteConfig() {

		final Intent intent = new Intent(this, ListaGenericaActivity.class);
		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();
		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.xml_defaults);

		long cacheExpiration = 3600; // 1 hour in seconds.
		// If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
		// the server.
		if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
			cacheExpiration = 0;
		}

		mFirebaseRemoteConfig.fetch(cacheExpiration)
				.addOnCompleteListener(this, new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							mFirebaseRemoteConfig.activateFetched();
						}
						Map<String, String> map = new HashMap<String, String>();
						map = getListaRemoteConfig();

						String numero = String.valueOf(new Random().nextInt(10));
						//Log.d(TAG, "numero : " + numero);
						//TODO mandar somente o Map e fazer a troca na outra activity
						intent.putExtra(Constantes.INSTANCE.getLISTA_ID(), map.get("id" + numero));
						intent.putExtra(Constantes.INSTANCE.getLISTA_GENERICA(), map.get("title" + numero));
						intent.putExtra(Constantes.INSTANCE.getBUNDLE(), (Serializable) map);

						startActivity(intent);

					}
				});
	}

	private Map<String, String> getListaRemoteConfig() {
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i <= 9; i++) {
			map.put("id" + i, mFirebaseRemoteConfig.getString("id" + i));
			map.put("title" + i, mFirebaseRemoteConfig.getString("title" + i));
		}
		return map;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case android.R.id.home: {
				if (drawerLayout != null) {
					openDrawer();
					return true;
				}
				break;
			}

			case R.id.search: {
				if (dialog == null) {
					View view = getLayoutInflater().inflate(R.layout.layout_search_multi, null);
					dialog = new AlertDialog.Builder(BaseActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
							.setView(view)
							.setCancelable(true)
							.create();
				}


				if (item.getIcon() != null) item.getIcon().setAlpha(10);

				dialog.show();

				SearchView searchViewDialog = dialog.findViewById(R.id.layout_search_multi_search);

				searchViewDialog.setIconifiedByDefault(false);
				searchViewDialog.setFocusable(true);
				searchViewDialog.setIconified(false);
				searchViewDialog.requestFocusFromTouch();

				searchViewDialog.setOnCloseListener(() -> {
					if (dialog != null && dialog.isShowing())
					dialog.dismiss();
					return false;
				});

				dialog.setOnDismissListener(dialog -> {
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
					);
					if (item.getIcon() != null) item.getIcon().setAlpha(255);
				});

				searchViewDialog.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						if (item.getIcon() != null) item.getIcon().setAlpha(255);
						Intent intent = new Intent(BaseActivity.this, SearchMultiActivity.class);
						intent.putExtra(SearchManager.QUERY, query);
						startActivity(intent);

						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						if (newText.isEmpty() || newText.length() < 2) return false;

						Subscription inscricao = new Api(BaseActivity.this)
								.procuraMulti(newText)
								.distinctUntilChanged()
								.debounce(1200, TimeUnit.MILLISECONDS)
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(new Observer<MultiSearch>() {
									@Override
									public void onCompleted() {

									}

									@Override
									public void onError(Throwable e) {
										Toast.makeText(BaseActivity.this, getString(R.string.ops), Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onNext(MultiSearch multiRetorno) {

										RecyclerView recyclerView = dialog.findViewById(R.id.layout_search_multi_recycler);
										recyclerView.setHasFixedSize(true);
										recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));
										recyclerView.setAdapter(new MultiAdapter(BaseActivity.this, multiRetorno, item.getIcon()));
									}
								});

						subscriptions.add(inscricao);
						return false;
					}
				});
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);//
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		subscriptions.unsubscribe();
	}

	protected void salvaImagemMemoriaCache(final Context context, final String endereco, final SalvarImageShare callback) {

		final ImageView imageView = new ImageView(context);
		Picasso.get().load(UtilsApp.getBaseUrlImagem(4) + endereco).into(imageView, new Callback() {
			@Override
			public void onSuccess() {
				File file = context.getExternalCacheDir();
				if (file != null)
					if (!file.exists()) {
						file.mkdir();

					}
				File dir = new File(file, endereco);
				BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
				if (drawable != null) {
					Bitmap bitmap = drawable.getBitmap();
					UtilsApp.writeBitmap(dir, bitmap);
				}
				callback.retornaFile(dir);
			}

			@Override
			public void onError(Exception e) {
				callback.RetornoFalha();
			}
		});
	}

	//Abre Menu Lateral
	private void openDrawer() {
		if (drawerLayout != null) {
			drawerLayout.openDrawer(GravityCompat.START);
		}
	}

	protected View.OnClickListener onClickListenerLogar() {
		return view -> startActivity(new Intent(BaseActivity.this, LoginActivity.class));
	}

	//Fecha Menu Lateral
	protected void closeDrawer() {
		if (drawerLayout != null) {
			drawerLayout.closeDrawer(GravityCompat.START);
		}

	}

	protected View.OnClickListener onClickListenerlogado() {
		return view -> {
			if (user != null) {
				final AlertDialog alertDialog = new AlertDialog
						.Builder(BaseActivity.this)
						.setView(R.layout.user)
						.create();
				alertDialog.show();

				ImageView user_img = (ImageView) alertDialog.findViewById(R.id.userImage);
				Picasso.get()
						.load(user.getPhotoUrl())
						.placeholder(R.drawable.user)
						.into(user_img);

				TextView email = (TextView) alertDialog.findViewById(R.id.text_user_email);
				email.setText(user.getEmail() != null ? user.getEmail() : "N/A");
				TextView uid = (TextView) alertDialog.findViewById(R.id.text_user_uid);
				uid.setText(user.getUid());
				TextView login = (TextView) alertDialog.findViewById(R.id.text_user_login);
				login.setText(user.getDisplayName() != null ? user.getDisplayName() : "N/A");
				Button reset = (Button) alertDialog.findViewById(R.id.bt_reset);
				Button desativar = (Button) alertDialog.findViewById(R.id.bt_desativar);
				Button vincular_login = (Button) alertDialog.findViewById(R.id.vincular_login);

				vincular_login.setOnClickListener(it -> startActivity(new Intent(BaseActivity.this, VincularLoginActivity.class)));

				desativar.setOnClickListener(it -> {

					AlertDialog dialog = new AlertDialog.Builder(BaseActivity.this)
							.setTitle(getResources().getString(R.string.deletar_conta))
							.setMessage(getResources().getString(R.string.deletar_conta_txt))
							.setNegativeButton(getResources().getString(R.string.cancel), null)
							.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									user.delete()
											.addOnCompleteListener(task -> {
												if (task.isSuccessful()) {
													Toast.makeText(BaseActivity.this, getResources().getString(R.string.conta_deletada), Toast.LENGTH_LONG)
															.show();
													FirebaseAuth.getInstance().signOut();
													finish();
													startActivity(new Intent(BaseActivity.this, LoginActivity.class));
												} else {
													Toast.makeText(BaseActivity.this, getResources().getString(R.string.falha_delete_conta), Toast.LENGTH_LONG)
															.show();
												}
											});
								}

							}).create();
					dialog.show();
				});

				reset.setOnClickListener(it -> {
					final AlertDialog alertDialogReset = new AlertDialog
							.Builder(BaseActivity.this)
							.setView(R.layout.reset_senha)
							.create();
					alertDialogReset.show();

					final TextInputLayout senha = (TextInputLayout) alertDialogReset.findViewById(R.id.pass);
					final TextInputLayout repetir_senha = (TextInputLayout) alertDialogReset.findViewById(R.id.repetir_pass);

					Button cancel = (Button) alertDialogReset.findViewById(R.id.bt_cancel);

					cancel.setOnClickListener(cancel_it -> alertDialogReset.dismiss());

					Button ok = (Button) alertDialogReset.findViewById(R.id.bt_ok);
					ok.setOnClickListener(ok_it -> {
						String senhaString = senha.getEditText().getText().toString();
						String repetirSenha = repetir_senha.getEditText().getText().toString();

						boolean tamanhoSenha = validatePassword(repetirSenha);
						boolean tamanhoRetiriSenha = validatePassword(senhaString);

						if (tamanhoSenha && tamanhoRetiriSenha
								&& senhaString.equals(repetirSenha)) {
							user.updatePassword(senhaString)
									.addOnCompleteListener(task -> {
										if (task.isSuccessful()) {
											alertDialogReset.dismiss();
											Toast.makeText(BaseActivity.this,
															getResources().getString(R.string.reset_senha_ok), Toast.LENGTH_LONG).show();
										} else {
											Toast.makeText(BaseActivity.this,
															getResources().getString(R.string.falha_reset_senha), Toast.LENGTH_LONG).show();
										}
									});
						} else {
							// Log.d(TAG, "Não entrou");
						}
					});
				});

				if (user.isAnonymous()) {
					reset.setVisibility(View.GONE);
					desativar.setVisibility(View.GONE);
					vincular_login.setVisibility(View.VISIBLE);
				}

			}
		};
	}

	public boolean validatePassword(String password) {
		return password.length() > 5;
	}

	protected boolean getIdioma(){
		try {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			return sharedPref.getBoolean(SettingsActivity.PREF_IDIOMA_PADRAO, true);
		} catch (Exception e) {
			Crashlytics.logException(e);
			return false;
		}
	}


	public interface SalvarImageShare {
		void retornaFile(File file);
		void RetornoFalha();
	}
}