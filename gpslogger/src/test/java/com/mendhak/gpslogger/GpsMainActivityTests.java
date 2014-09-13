package com.geotrackin.gpslogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.webkit.WebView;
import com.geotrackin.gpslogger.senders.email.AutoEmailActivity;
import com.geotrackin.gpslogger.senders.ftp.AutoFtpActivity;
import com.robotium.solo.Solo;


public class GpsMainActivityTests extends ActivityInstrumentationTestCase2<GpsMainActivity> {

    private Solo solo;

    public GpsMainActivityTests(){
        super(GpsMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Context context = getInstrumentation().getTargetContext();
        final SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferencesEditor.putBoolean("navigation_drawer_learned", true);
        preferencesEditor.commit();

        solo = new Solo(getInstrumentation(), getActivity());

    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    @MediumTest
    public void testCanSeeMenuItems() {
        solo.sendKey(Solo.MENU);
        assertTrue("Could not find menu Logging Settings", solo.searchText(solo.getCurrentActivity().getString(R.string.title_drawer_loggingsettings)));
        assertTrue("Could not find menu Upload Settings", solo.searchText(solo.getCurrentActivity().getString(R.string.title_drawer_uploadsettings)));
        assertTrue("Could not find menu General Settings", solo.searchText(solo.getCurrentActivity().getString(R.string.title_drawer_generalsettings)));
    }

    @MediumTest
    public void testHelpButtonOpensFAQPage(){
        solo.clickOnView(solo.getView(R.id.imgHelp));
        solo.assertCurrentActivity("FAQ Screen", Faqtivity.class);
        WebView webView = (WebView)solo.getView(R.id.faqwebview);
        //assertTrue(solo.getWebUrl(), solo.getWebUrl().equalsIgnoreCase("http://code.mendhak.com/gpslogger/index.html"));
        assertTrue(solo.getWebUrl(), solo.getWebUrl().equalsIgnoreCase("http://geotrack.in/faq/index.html"));
    }

    @MediumTest
    public void testSinglePointButtonDisabledWhenLoggingStarted() {
        //setDrawerVisibility(false);
        solo.setNavigationDrawer(Solo.CLOSED);
        solo.clickOnView(solo.getView(R.id.simple_play));
        solo.sleep(500);
        assertFalse("One Point button should be disabled if main logging enabled", ((GpsMainActivity) solo.getCurrentActivity()).mnuOnePoint.isEnabled());
        solo.clickOnView(solo.getView(R.id.simple_stop));
        solo.sleep(500);
        assertTrue("One Point button should be enabled if main logging stopped",  ((GpsMainActivity) solo.getCurrentActivity()).mnuOnePoint.isEnabled());
        solo.finishOpenedActivities();
    }


    @MediumTest
    public void testAnnotateButtonDisabledIfGpxKmlUrlAreDisabled(){

        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.title_drawer_loggingsettings));
        solo.scrollToTop();
        if(solo.isCheckBoxChecked(0)) { solo.clickOnCheckBox(0); }
        if(solo.isCheckBoxChecked(1)) { solo.clickOnCheckBox(1); }
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.log_customurl_title));
        if(solo.isCheckBoxChecked(0)) { solo.clickOnCheckBox(0); }
        solo.goBack();
        solo.goBack();
        assertFalse("Annotate button should be disabled if GPX, KML, URL disabled",
                ((GpsMainActivity) solo.getCurrentActivity()).mnuAnnotate.isEnabled());
    }

    @MediumTest
    public void testFilePathNotShownIfGpxKmlCsvAreDisabled(){

        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.title_drawer_loggingsettings));
        solo.scrollToTop();
        if(solo.isCheckBoxChecked(0)) { solo.clickOnCheckBox(0); }
        if(solo.isCheckBoxChecked(1)) { solo.clickOnCheckBox(1); }
        if(solo.isCheckBoxChecked(3)) { solo.clickOnCheckBox(3); }
        solo.goBack();

        assertFalse("File path should be hidden if GPX, KML and CSV disabled",
                solo.getCurrentActivity().findViewById(R.id.simpleview_txtfilepath).isShown());
    }

    @MediumTest
    public void testSpinnerNavigation(){
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.view_simple));
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.view_detailed));
        solo.finishOpenedActivities();

        launchActivity("com.geotrackin.gpslogger", GpsMainActivity.class,null);

        //solo.waitForFragmentById(R.layout.fragment_detailed_view);
        assertTrue("Detailed view should be visible if previously selected",
                solo.getView(R.id.detailedview_lat_label).isShown());

        solo.clickOnText(solo.getCurrentActivity().getString(R.string.view_detailed));
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.view_simple));

    }

    @MediumTest
    public void testAutoEmailsRequireFilledValues() {
        launchActivity("com.geotrackin.gpslogger", AutoEmailActivity.class, null);
        solo.clickOnCheckBox(0);
        solo.goBack();
        assertTrue("Email form without valid values should show alert dialog", solo.searchText(solo.getCurrentActivity().getString(R.string.autoemail_invalid_form)));
        solo.clickOnText("OK");
        assertTrue("Enable emails checkbox should be unchecked", !solo.isCheckBoxChecked(0));
    }

    @MediumTest
    public void testAutoFtpRequireFilledValues() {
        launchActivity("com.geotrackin.gpslogger", AutoFtpActivity.class, null);
        solo.clickOnCheckBox(0);
        solo.goBack();
        assertTrue("FTP form without valid values should show alert dialog", solo.searchText(solo.getCurrentActivity().getString(R.string.autoemail_invalid_form)));
        solo.clickOnText("OK");
        assertTrue("Enable FTP checkbox should be unchecked", !solo.isCheckBoxChecked(0));
    }

    @MediumTest
    public void testClickingSimpleViewIconProducesToast(){
        solo.clickOnView(solo.getView(R.id.simpleview_imgSpeed));
        assertTrue(solo.waitForText("Speed"));
    }

    @MediumTest
    public void testLoggingPausedWhenAbsoluteTimeoutReached(){
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.title_drawer_loggingsettings));
        solo.scrollToBottom();
        solo.clickOnText(solo.getCurrentActivity().getString(R.string.absolute_timeout_title));
        solo.clearEditText(0);
        solo.enterText(0, "10");
        solo.clickOnText("OK");
        solo.goBack();
        solo.clickOnView(solo.getView(R.id.simple_play));
        solo.sleep(500);
        assertTrue("Progress gif should spin when logging started", solo.getCurrentActivity().findViewById(R.id.progressBarGpsFix).isShown());
        solo.sleep(11000);
        assertFalse("Absolute timeout should stop waiting for a fix after 10 seconds", solo.getCurrentActivity().findViewById(R.id.progressBarGpsFix).isShown());
        solo.clickOnView(solo.getView(R.id.simple_stop));

    }

}