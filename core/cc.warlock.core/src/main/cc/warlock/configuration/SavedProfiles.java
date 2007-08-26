package cc.warlock.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Marshall
 */
public class SavedProfiles {
	private static Properties props = new Properties();
	
	private static Hashtable<String, Account> accounts = new Hashtable<String, Account>();
	private static Hashtable<Account, ArrayList<Profile>> profiles = new Hashtable<Account, ArrayList<Profile>>();
	
	public static final String FILENAME = "profiles.properties";
	
	private static final String SAVED_ACCOUNTS = "warlock.saved.accounts";
	private static final String SAVED_PROFILES = "warlock.saved.profiles";
	private static final String ACCOUNT_PROPERTY_PREFIX = "warlock.account.";
	private static final String PROFILE_PROPERTY_PREFIX = "warlock.profile.";
	private static final String ACCOUNT_NAME = "accountName";
	private static final String PASSWORD = "password";
	private static final String CHARACTER_CODE = "characterCode";
	private static final String CHARACTER_NAME = "characterName";
	private static final String GAME_CODE = "gameCode";
	private static final String GAME_NAME = "gameName";
	
	static {
			try {
				FileInputStream stream = new FileInputStream(WarlockConfiguration.getConfigurationFile(FILENAME));
				props.load(stream);
				stream.close();
				
				String[] savedAccountIds = getSavedAccountIds();
				for (String id : savedAccountIds)
				{
					Account account = new Account();
					account.setAccountId(id);
					account.setAccountName(getAccountProperty(id, ACCOUNT_NAME));
					account.setPassword(getAccountProperty(id, PASSWORD));
					
					accounts.put(getAccountProperty(id, ACCOUNT_NAME), account);
					profiles.put(account, new ArrayList<Profile>());
				}
				
				String[] profileIds = getSavedProfileIds();
				for (String id : profileIds)
				{
					Account account = accounts.get(getProfileProperty(id , ACCOUNT_NAME));
					Profile profile = new Profile();
					profile.setProfileId(id);
					profile.setAccount(account);
					profile.setCharacterCode(getProfileProperty(id, CHARACTER_CODE));
					profile.setCharacterName(getProfileProperty(id, CHARACTER_NAME));
					profile.setGameCode(getProfileProperty(id, GAME_CODE));
					profile.setGameName(getProfileProperty(id, GAME_NAME));
					
					profiles.get(account).add(profile);
				}
			} catch (Exception e) {
				// swallow for now
			}
	}

	public static void save ()
	{
		try {
			props.clear();
			
			boolean first = true;
			String accountIds = "";
			for (Account account : accounts.values())
			{
				if (!first) {
					accountIds += ",";
				} else first = false;
				
				accountIds += account.getAccountId();
				
				setAccountProperty(account.getAccountId(), ACCOUNT_NAME, account.getAccountName());
				setAccountProperty(account.getAccountId(), PASSWORD, account.getPassword());
			}
			props.setProperty (SAVED_ACCOUNTS, accountIds);
			
			first = true;
			String profileIds = "";
			for (ArrayList<Profile> profileCollection : profiles.values())
			{
				for (Profile profile : profileCollection)
				{
					if (!first) {
						profileIds += ",";
					} else first = false;
					
					profileIds += profile.getProfileId();
					
					setProfileProperty(profile.getProfileId(), ACCOUNT_NAME, profile.getAccount().getAccountName());
					setProfileProperty (profile.getProfileId(), CHARACTER_CODE, profile.getCharacterCode());
					setProfileProperty (profile.getProfileId(), CHARACTER_NAME, profile.getCharacterName());
					setProfileProperty (profile.getProfileId(), GAME_CODE, profile.getGameCode());
					setProfileProperty (profile.getProfileId(), GAME_NAME, profile.getGameName());
				}
				props.setProperty (SAVED_PROFILES, profileIds);
			}
			
			FileOutputStream stream = new FileOutputStream(WarlockConfiguration.getConfigurationFile(FILENAME));
			props.store(stream, "Generated by Warlock 2.0");
			stream.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<Account> getAccounts ()
	{
		return accounts.values();
	}
	
	public static Account getAccount (String accountName)
	{
		return accounts.get(accountName);
	}
	
	public static Account addAccount (String accountName, String password)
	{
		Account account = new Account();
		int id = accounts.values().size()+1;
		
		account.setAccountId(id+"");
		account.setAccountName(accountName);
		account.setPassword(password);
		accounts.put(accountName, account);
		
		save();
		return account;
	}
	
	public static void removeAccount (String accountName)
	{
		accounts.remove(accountName);
		
		save();
	}
	
	public static Profile addProfile (Account account, String characterCode, String characterName, String gameCode, String gameName)
	{
		Profile profile = new Profile();
		int id = getAllProfiles().size() + 1;
		
		profile.setProfileId(id+"");
		profile.setAccount(account);
		profile.setCharacterCode(characterCode);
		profile.setCharacterName(characterName);
		profile.setGameCode(gameCode);
		profile.setGameName(gameName);
		
		if (!profiles.containsKey(account)) {
			profiles.put(account, new ArrayList<Profile>());
		}
		profiles.get(account).add(profile);
		
		save();
		return profile;
	}
	
	public static Collection<Profile> getAllProfiles ()
	{
		ArrayList<Profile> allProfiles = new ArrayList<Profile>();
		for (Account acct : accounts.values())
		{
			if (profiles.containsKey(acct))
				allProfiles.addAll(profiles.get(acct));
		}
		
		return allProfiles;
	}
	
	public static Collection<Profile> getProfiles (Account account)
	{
		return profiles.get(account);
	}
	
	public static Profile getProfileByCharacterName (String characterName)
	{
		for (ArrayList<Profile> profiles2 : profiles.values())
		{
			for (Profile profile : profiles2)
			{
				if (profile.getCharacterName().equals(characterName))
					return profile;
			}
		}
		return null;
	}
	
	private static String[] getSavedAccountIds ()
	{
		String savedAccounts = props.getProperty(SAVED_ACCOUNTS);
		String savedAccountIds[];
		if( savedAccounts != null ) {
			savedAccountIds = savedAccounts.split(",");
		} else {
			savedAccountIds = new String[0];
		}	
		
		return savedAccountIds;
	}
	
	private static String[] getSavedProfileIds ()
	{
		String savedProfiles = props.getProperty(SAVED_PROFILES);
		String savedProfileIds[];
		if (savedProfiles != null)
			savedProfileIds = savedProfiles.split(",");
		else
			savedProfileIds = new String[0];
		
		return savedProfileIds;
	}
	
	private static String getProfileProperty (String profileId, String propertyName)
	{
		return props.getProperty(PROFILE_PROPERTY_PREFIX + profileId + "." + propertyName);
	}
	
	private static String getAccountProperty (String accountId, String propertyName)
	{
		return props.getProperty(ACCOUNT_PROPERTY_PREFIX + accountId + "." + propertyName);
	}
	
	private static void setProfileProperty (String profileId, String propertyName, String value)
	{
		props.setProperty(PROFILE_PROPERTY_PREFIX + profileId + "." + propertyName, value);
	}
	
	private static void setAccountProperty (String accountId, String propertyName, String value)
	{
		props.setProperty(ACCOUNT_PROPERTY_PREFIX + accountId + "." + propertyName, value);
	}
}
