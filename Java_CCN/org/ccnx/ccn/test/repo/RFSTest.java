package org.ccnx.ccn.test.repo;

import java.io.File;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.ccnx.ccn.impl.repo.RFSLogImpl;
import org.ccnx.ccn.impl.repo.Repository;
import org.ccnx.ccn.impl.repo.RepositoryException;
import org.ccnx.ccn.impl.repo.Repository.NameEnumerationResponse;
import org.ccnx.ccn.impl.support.DataUtils;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.profiles.nameenum.CCNNameEnumerator;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.KeyLocator;
import org.ccnx.ccn.protocol.PublisherID;
import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;
import org.ccnx.ccn.protocol.SignedInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author rasmusse
 * 
 * Test repository backend implementation(s) using filesystem (FS) as stable storage.  In principle,
 * there could be multiple FS-backed implementations exercised by these tests.
 * 
 * Because it uses the default KeyManager, this test must be run
 * with ccnd running.
 *
 */

public class RFSTest extends RepoTestBase {
	
	Repository repolog; // Instance of simple log-based repo implementation under test
	
	private ContentName longName;
	private ContentName badCharName;
	private ContentName badCharLongName;
	private ContentName versionedName;
	private ContentName segmentedName1;
	private ContentName segmentedName223;
	private ContentName versionedNameNormal;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RepoTestBase.setUpBeforeClass();
		_fileTest = new File(_fileTestDir);
		DataUtils.deleteDirectory(_fileTest);
		_fileTest.mkdirs();
	}
		
	@Before
	public void setUp() throws Exception {
		super.setUp();
		initRepoLog();
	}
						
	public void initRepoLog() throws Exception {
		repolog = new RFSLogImpl();
		repolog.initialize(new String[] {"-root", _fileTestDir, "-local", _repoName, "-global", _globalPrefix, "-singlefile"}, putLibrary);
	}
	
	@Test
	public void testRepo() throws Exception {
		System.out.println("testing repo (log-structured implementation)");
		test(repolog);
		initRepoLog();
		// Having initialized a new instance on the same stable storage stage produced by the
		// test() method, now run testReinitialization to check consistency.
		testReinitialization(repolog);
	}
	
	
	public void test(Repository repo) throws Exception{		
		System.out.println("Repotest - Testing basic data");
		ContentName name = ContentName.fromNative("/repoTest/data1");
		ContentObject content = ContentObject.buildContentObject(name, "Here's my data!".getBytes());
		repo.saveContent(content);
		checkData(repo, name, "Here's my data!");
		
		// TODO - Don't know how to check that multiple data doesn't result in multiple copies
		// Do it just to make sure the mechanism doesn't break (but result is not tested).
		repo.saveContent(content);
				
		System.out.println("Repotest - Testing multiple digests for same data");
		ContentObject digest2 = ContentObject.buildContentObject(name, "Testing2".getBytes());
		repo.saveContent(digest2);
		ContentName digestName = new ContentName(name, digest2.contentDigest());
		checkDataWithDigest(repo, digestName, "Testing2");
		
		System.out.println("Repotest - Testing same digest for different data and/or publisher");
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(512); // go for fast
		KeyPair pair1 = kpg.generateKeyPair();
		PublisherPublicKeyDigest pubKey1 = new PublisherPublicKeyDigest(pair1.getPublic());
		KeyLocator kl = new KeyLocator(new ContentName(keyprefix, pubKey1.digest()));
		repo.saveContent(ContentObject.buildContentObject(kl.name().name(), pubKey1.digest()));
		SignedInfo si = new SignedInfo(pubKey1, kl);
		ContentObject digestSame1 = new ContentObject(name, si, "Testing2".getBytes(), pair1.getPrivate());
		repo.saveContent(digestSame1);
		KeyPair pair2 = kpg.generateKeyPair();
		PublisherPublicKeyDigest pubKey2 = new PublisherPublicKeyDigest(pair2.getPublic());
		kl = new KeyLocator(new ContentName(keyprefix, pubKey2.digest()));
		repo.saveContent(ContentObject.buildContentObject(kl.name().name(), pubKey2.digest()));
		si = new SignedInfo(pubKey2, kl);
		ContentObject digestSame2 = new ContentObject(name, si, "Testing2".getBytes(), pair2.getPrivate());
		repo.saveContent(digestSame2);
		checkDataAndPublisher(repo, name, "Testing2", pubKey1);
		checkDataAndPublisher(repo, name, "Testing2", pubKey2);
		
		System.out.println("Repotest - Testing too long data");
		String tooLongName = "0123456789";
		for (int i = 0; i < 30; i++)
			tooLongName += "0123456789";
		longName = ContentName.fromNative("/repoTest/" + tooLongName);
		repo.saveContent(ContentObject.buildContentObject(longName, "Long name!".getBytes()));
		checkData(repo, longName, "Long name!");
		digest2 = ContentObject.buildContentObject(longName, "Testing2".getBytes());
		repo.saveContent(digest2);
		digestName = new ContentName(longName, digest2.contentDigest());
		checkDataWithDigest(repo, digestName, "Testing2");
		String wayTooLongName = tooLongName;
		for (int i = 0; i < 30; i++)
			wayTooLongName += "0123456789";
		ContentName reallyLongName = ContentName.fromNative("/repoTest/" + wayTooLongName);
		repo.saveContent(ContentObject.buildContentObject(reallyLongName, "Really Long name!".getBytes()));
		checkData(repo, reallyLongName, "Really Long name!");
		byte[][] longNonASCIIBytes = new byte[2][];
		longNonASCIIBytes[0] = "repoTest".getBytes();
		longNonASCIIBytes[1] = new byte[300];
		
		for (int i = 0; i < 30; i++) {
			rand.nextBytes(longNonASCIIBytes[1]);
			ContentName lnab = new ContentName(longNonASCIIBytes);
			repo.saveContent(ContentObject.buildContentObject(lnab, ("Long and Non ASCII " + i).getBytes()));
			checkData(repo, lnab, "Long and Non ASCII " + i);
		}
		
		System.out.println("Repotest - Testing invalid characters in name");
		badCharName = ContentName.fromNative("/repoTest/" + "*x?y<z>u");
		repo.saveContent(ContentObject.buildContentObject(badCharName, "Funny characters!".getBytes()));
		checkData(repo, badCharName, "Funny characters!");
		badCharLongName = ContentName.fromNative("/repoTest/" + tooLongName + "*x?y<z>u");
		repo.saveContent(ContentObject.buildContentObject(badCharLongName, "Long and funny".getBytes()));
		checkData(repo, badCharLongName, "Long and funny");
		
		System.out.println("Repotest - Testing different kinds of interests");
		ContentName name1 = ContentName.fromNative("/repoTest/nextTest/aaa");
		ContentObject content1 = ContentObject.buildContentObject(name1, "aaa".getBytes());
		repo.saveContent(content1);
		ContentName name2 = ContentName.fromNative("/repoTest/nextTest/bbb");
		repo.saveContent(ContentObject.buildContentObject(name2, "bbb".getBytes()));
		ContentName name3= ContentName.fromNative("/repoTest/nextTest/ccc");
		repo.saveContent(ContentObject.buildContentObject(name3, "ccc".getBytes()));
		ContentName name4= ContentName.fromNative("/repoTest/nextTest/ddd");
		repo.saveContent(ContentObject.buildContentObject(name4, "ddd".getBytes()));
		ContentName name5= ContentName.fromNative("/repoTest/nextTest/eee");
		repo.saveContent(ContentObject.buildContentObject(name5, "eee".getBytes()));
		checkData(repo, Interest.next(new ContentName(name1, content1.contentDigest()), 2), "bbb");
		checkData(repo, Interest.last(new ContentName(name1, content1.contentDigest()), 2), "eee");
		checkData(repo, Interest.next(new ContentName(name1, content1.contentDigest()), 
				new byte [][] {"bbb".getBytes(), "ccc".getBytes()}, 2), "ddd");
		
		System.out.println("Repotest - Testing different kinds of interests in a mixture of encoded/standard data");
		ContentName nonLongName = ContentName.fromNative("/repoTestLong/nextTestLong/aaa");
		ContentObject nonLongContent = ContentObject.buildContentObject(nonLongName, "aaa".getBytes());
		repo.saveContent(nonLongContent);
		ContentName longName2 = ContentName.fromNative("/repoTestLong/nextTestLong/bbb/" + tooLongName);
		repo.saveContent(ContentObject.buildContentObject(longName2, "bbb".getBytes()));
		ContentName nonLongName2 = ContentName.fromNative("/repoTestLong/nextTestLong/ccc");
		repo.saveContent(ContentObject.buildContentObject(nonLongName2, "ccc".getBytes()));
		ContentName longName3 = ContentName.fromNative("/repoTestLong/nextTestLong/ddd/" + tooLongName);
		repo.saveContent(ContentObject.buildContentObject(longName3, "ddd".getBytes()));
		ContentName longName4 = ContentName.fromNative("/repoTestLong/nextTestLong/eee/" + tooLongName);
		repo.saveContent(ContentObject.buildContentObject(longName4, "eee".getBytes()));
		checkData(repo, Interest.next(new ContentName(nonLongName, nonLongContent.contentDigest()), 2), "bbb");
		checkData(repo, Interest.last(new ContentName(nonLongName, nonLongContent.contentDigest()), 2), "eee");
		checkData(repo, Interest.next(new ContentName(nonLongName, nonLongContent.contentDigest()), 
				new byte [][] {"bbb".getBytes(), "ccc".getBytes()}, 2), "ddd");
		
		System.out.println("Repotest - testing version and segment files");
		versionedName = ContentName.fromNative("/repoTest/testVersion");
		versionedName = VersioningProfile.addVersion(versionedName);
		repo.saveContent(ContentObject.buildContentObject(versionedName, "version".getBytes()));
		checkData(repo, versionedName, "version");
		segmentedName1 = SegmentationProfile.segmentName(versionedName, 1);
		repo.saveContent(ContentObject.buildContentObject(segmentedName1, "segment1".getBytes()));
		checkData(repo, segmentedName1, "segment1");
		segmentedName223 = SegmentationProfile.segmentName(versionedName, 223);
		repo.saveContent(ContentObject.buildContentObject(segmentedName223, "segment223".getBytes()));
		checkData(repo, segmentedName223, "segment223");
		
		System.out.println("Repotest - storing sequence of objects for versioned stream read testing");
		versionedNameNormal = ContentName.fromNative("/testNameSpace/testVersionNormal");
		versionedNameNormal = VersioningProfile.addVersion(versionedNameNormal);
		repo.saveContent(ContentObject.buildContentObject(versionedNameNormal, "version-normal".getBytes()));
		checkData(repo, versionedNameNormal, "version-normal");
		byte[] finalBlockID = SegmentationProfile.getSegmentID(4);
		for (Long i=SegmentationProfile.baseSegment(); i<5; i++) {
			ContentName segmented = SegmentationProfile.segmentName(versionedNameNormal, i);
			String segmentContent = "segment"+ new Long(i).toString();
			repo.saveContent(ContentObject.buildContentObject(segmented, segmentContent.getBytes(), null, null, finalBlockID));
			checkData(repo, segmented, segmentContent);
		}

		//adding in fast name enumeration response tests
		System.out.println("Repotest - testing fast name enumeration response");

		//building names for tests
		ContentName nerpre = ContentName.fromNative("/testFastNameEnumeration");
		ContentName ner = new ContentName(nerpre, "name1".getBytes());
		ContentName nername1 = ContentName.fromNative("/name1");
		ContentName ner2 = new ContentName(nerpre, "name2".getBytes());
		ContentName nername2 = ContentName.fromNative("/name2");
		ContentName ner3 = new ContentName(nerpre, "longer".getBytes());
		ner3 = new ContentName(ner3, "name3".getBytes());
		ContentName nername3 = ContentName.fromNative("/longer");
		NameEnumerationResponse neresponse = null;

		//send initial interest to make sure namespace is empty
		//interest flag will not be set for a fast response since there isn't anything in the index yet
		
		Interest interest = new Interest(new ContentName(nerpre, CCNNameEnumerator.NEMARKER));
		neresponse = repo.getNamesWithPrefix(interest);
		Assert.assertTrue(neresponse == null || neresponse.getNames()==null);
		//now saving the first piece of content in the repo.  interest flag not set, so it should not get an object back
		neresponse = repo.saveContent(ContentObject.buildContentObject(ner, "FastNameRespTest".getBytes()));
		Assert.assertTrue(neresponse==null || neresponse.getNames()==null);
		//now checking with the prefix that the first name is in
		neresponse = repo.getNamesWithPrefix(interest);
		Assert.assertTrue(neresponse.getNames().contains(nername1));

		Assert.assertTrue(neresponse.getPrefix().contains(CCNNameEnumerator.NEMARKER));
		Assert.assertTrue(neresponse.getTimestamp()!=null);
		//now call get names with prefix again to set interest flag
		//have to use the version from the last response (or at least a version after the last write
		interest = Interest.last(VersioningProfile.addVersion(neresponse.getPrefix(), neresponse.getTimestamp()));
		//the response should be null and the flag set
		neresponse = repo.getNamesWithPrefix(interest);
		Assert.assertTrue(neresponse==null || neresponse.getNames()==null);
		//save content.  if the flag was set, we should get an enumeration response
		neresponse = repo.saveContent(ContentObject.buildContentObject(ner2, "FastNameRespTest".getBytes()));
		Assert.assertTrue(neresponse.getNames().contains(nername1));
		Assert.assertTrue(neresponse.getNames().contains(nername2));
		Assert.assertTrue(neresponse.getPrefix().contains(CCNNameEnumerator.NEMARKER));
		Assert.assertTrue(neresponse.getTimestamp()!=null);
		
		//need to reconstruct the interest again
		interest = Interest.last(VersioningProfile.addVersion(neresponse.getPrefix(), neresponse.getTimestamp()));
		//another interest to set interest flag, response should be null
		neresponse = repo.getNamesWithPrefix(interest);
		Assert.assertTrue(neresponse == null || neresponse.getNames()==null);
		//interest flag should now be set, so when i add something - this is a longer name, i should be handed back an object
		neresponse = repo.saveContent(ContentObject.buildContentObject(ner3, "FastNameRespTest".getBytes()));
		Assert.assertTrue(neresponse.getNames().contains(nername1));
		Assert.assertTrue(neresponse.getNames().contains(nername2));
		Assert.assertTrue(neresponse.getNames().contains(nername3));
		Assert.assertTrue(neresponse.getPrefix().contains(CCNNameEnumerator.NEMARKER));
		Assert.assertTrue(neresponse.getTimestamp()!=null);
		
		repo.shutDown();
	}
	
	public void testReinitialization(Repository repo) throws Exception {
		
		System.out.println("Repotest - Testing reinitialization of repo");
		// Since we have 2 pieces of data with the name "longName" we need to compute the
		// digest to make sure we get the right data.
		longName = new ContentName(longName, ContentObject.contentDigest("Long name!"));
		checkDataWithDigest(repo, longName, "Long name!");
		checkData(repo, badCharName, "Funny characters!");
		checkData(repo, badCharLongName, "Long and funny");
		Interest vnInterest = new Interest(versionedName);
		vnInterest.maxSuffixComponents(1);
		checkData(repo, vnInterest, "version");
		checkData(repo, segmentedName1, "segment1");
		checkData(repo, segmentedName223, "segment223");
		vnInterest = new Interest(versionedNameNormal);
		vnInterest.maxSuffixComponents(1);
		checkData(repo, vnInterest, "version-normal");
		for (Long i=SegmentationProfile.baseSegment(); i<5; i++) {
			ContentName segmented = SegmentationProfile.segmentName(versionedNameNormal, i);
			String segmentContent = "segment"+ new Long(i).toString();
			checkData(repo, segmented, segmentContent);
		}
	}
	
	@Test
	public void testPolicy() throws Exception {
		Repository repo = new RFSLogImpl();
		try {	// Test no version
			repo.initialize(new String[] {"-root", _fileTestDir, "-policy", _topdir + "/org/ccnx/ccn/test/repo/badPolicyTest1.xml"}, putLibrary);
			Assert.fail("Bad policy file succeeded");
		} catch (InvalidParameterException ipe) {}
		try {	// Test bad version
			repo.initialize(new String[] {"-root", _fileTestDir, "-policy", _topdir + "/org/ccnx/ccn/test/repo/badPolicyTest2.xml"}, putLibrary);
			Assert.fail("Bad policy file succeeded");
		} catch (InvalidParameterException ipe) {}
		repo.initialize(new String[] {"-root", _fileTestDir, "-policy", 
					_topdir + "/org/ccnx/ccn/test/repo/policyTest.xml", "-local", _repoName, "-global", _globalPrefix}, putLibrary);
		ContentName name = ContentName.fromNative("/testNameSpace/data1");
		ContentObject content = ContentObject.buildContentObject(name, "Here's my data!".getBytes());
		repo.saveContent(content);
		checkData(repo, name, "Here's my data!");
		ContentName outOfNameSpaceName = ContentName.fromNative("/anotherNameSpace/data1");
		ContentObject oonsContent = ContentObject.buildContentObject(outOfNameSpaceName, "Shouldn't see this".getBytes());
		repo.saveContent(oonsContent);
		ContentObject testContent = repo.getContent(new Interest(outOfNameSpaceName));
		Assert.assertTrue(testContent == null);
		repo.initialize(new String[] {"-root", _fileTestDir, "-policy", 
				_topdir + "/org/ccnx/ccn/test/repo/origPolicy.xml", "-local", _repoName, "-global", _globalPrefix}, putLibrary);
		repo.saveContent(oonsContent);
		checkData(repo, outOfNameSpaceName, "Shouldn't see this");	
	}
	
	private void checkData(Repository repo, ContentName name, String data) throws RepositoryException {
		checkData(repo, new Interest(name), data);
	}
	
	private void checkDataWithDigest(Repository repo, ContentName name, String data) throws RepositoryException {
		// When generating an Interest for the exact name with content digest, need to set maxSuffixComponents
		// to 0, signifying that name ends with explicit digest
		Interest interest = new Interest(name);
		interest.maxSuffixComponents(0);
		checkData(repo, interest, data);
	}

	private void checkData(Repository repo, Interest interest, String data) throws RepositoryException {
		ContentObject testContent = repo.getContent(interest);
		Assert.assertFalse(testContent == null);
		Assert.assertEquals(data, new String(testContent.content()));		
	}
	
	private void checkDataAndPublisher(Repository repo, ContentName name, String data, PublisherPublicKeyDigest publisher) 
				throws RepositoryException {
		Interest interest = new Interest(name, new PublisherID(publisher));
		ContentObject testContent = repo.getContent(interest);
		Assert.assertFalse(testContent == null);
		Assert.assertEquals(data, new String(testContent.content()));
		Assert.assertTrue(testContent.signedInfo().getPublisherKeyID().equals(publisher));
	}
}