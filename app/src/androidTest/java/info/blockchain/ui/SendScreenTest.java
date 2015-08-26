package info.blockchain.ui;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.robotium.solo.Solo;

import info.blockchain.ui.util.UiUtil;
import info.blockchain.wallet.HDPayloadBridge;
import info.blockchain.wallet.MainActivity;
import info.blockchain.wallet.multiaddr.MultiAddrFactory;
import info.blockchain.wallet.payload.Account;
import info.blockchain.wallet.payload.ImportedAccount;
import info.blockchain.wallet.send.SendFactory;
import info.blockchain.wallet.util.AccountsUtil;
import info.blockchain.wallet.util.MonetaryUtil;
import piuk.blockchain.android.R;

public class SendScreenTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo = null;

    private static boolean loggedIn = false;

    public SendScreenTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {

        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());

        if(!loggedIn){
            UiUtil.getInstance(getActivity()).enterPin(solo, solo.getString(R.string.qa_test_pin1));
            try{solo.sleep(4000);}catch (Exception e){}
            loggedIn = true;
        }

        solo.clickOnView(solo.getView(R.id.btActivateBottomSheet));
        try{solo.sleep(500);}catch (Exception e){}
        solo.clickOnText(getActivity().getString(R.string.send_bitcoin));
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    private void exitApp(){
        solo.goBack();
        solo.goBack();
        solo.goBack();
    }

    public void testA_SelectFromAccounts() throws AssertionError{

        Spinner mSpinner = solo.getView(Spinner.class, 0);
        int itemCount = mSpinner.getAdapter().getCount();
        View spinnerView = solo.getView(Spinner.class, 0);

        for(int i = 0; i < itemCount; i++){

            solo.clickOnView(spinnerView);
            solo.scrollToTop();
            TextView tvMax = (TextView)solo.getView(LinearLayout.class, i).findViewById(R.id.receive_account_balance);
            solo.clickOnView(solo.getView(LinearLayout.class, i));
            try{solo.sleep(500);}catch (Exception e){}

            //Test if amount available correct
            long amount = 0L;

            //Index of last hd account in spinner
            int hdAccountsIdx = AccountsUtil.getInstance(getActivity()).getLastHDIndex();

            //Legacy addresses
            if(i >= hdAccountsIdx) {
                amount = MultiAddrFactory.getInstance().getLegacyBalance(AccountsUtil.getInstance(getActivity()).getLegacyAddress(i - hdAccountsIdx).getAddress());
            }
            else {
                //HD Accounts
                String xpub = account2Xpub(i);
                if(MultiAddrFactory.getInstance().getXpubAmounts().containsKey(xpub)) {
                    amount = MultiAddrFactory.getInstance().getXpubAmounts().get(xpub);
                }
                else {
                    amount = 0L;
                }
            }

            String expectedTextTotal = "0.0";
            String expectedTextAvailable = getActivity().getResources().getString(R.string.no_funds_available);
            long amount_available = amount - SendFactory.bFee.longValue();
            long amount_total = amount;

            if(amount_available > 0L) {
                double btc_balance = (((double)amount_available) / 1e8);
                expectedTextAvailable = getActivity().getResources().getText(R.string.max_available) + " " + MonetaryUtil.getInstance().getBTCFormat().format(MonetaryUtil.getInstance(getActivity()).getDenominatedAmount(btc_balance));
            }

            if(amount_total > 0L) {
                double btc_total = (((double)amount_total) / 1e8);
                expectedTextTotal = MonetaryUtil.getInstance().getBTCFormat().format(MonetaryUtil.getInstance(getActivity()).getDenominatedAmount(btc_total));
            }


            String spinnerTextTotal = tvMax.getText().toString();
            spinnerTextTotal = spinnerTextTotal.replace("(", "").trim();
            spinnerTextTotal = spinnerTextTotal.replace(")", "").trim();
            CharSequence[] units = MonetaryUtil.getInstance().getBTCUnits();
            for(CharSequence unit : units){
                spinnerTextTotal = spinnerTextTotal.replace(unit.toString(), "").trim();
            }

            TextView tvMax2 = (TextView)solo.getCurrentActivity().findViewById(R.id.max);
            String available = tvMax2.getText().toString();
            for(CharSequence unit : units)
                available = available.replace(unit.toString(), "").trim();

            //Test if spinner totals matches actual account total
            assertTrue(expectedTextTotal.equals(spinnerTextTotal));

            //Test if max available matches actual available for account (total - fee)
            assertTrue(expectedTextAvailable.equals(available));
        }
    }

    private String account2Xpub(int sel) {

        Account hda = AccountsUtil.getInstance(getActivity()).getSendReceiveAccountMap().get(AccountsUtil.getInstance(getActivity()).getSendReceiveAccountIndexResolver().get(sel));
        String xpub = null;
        if(hda instanceof ImportedAccount) {
            xpub = null;
        }
        else {
            xpub = HDPayloadBridge.getInstance(getActivity()).account2Xpub(AccountsUtil.getInstance(getActivity()).getSendReceiveAccountIndexResolver().get(sel));
        }

        return xpub;
    }

    public void testB_InputToAddressManually() throws AssertionError{

        EditText toAddress = (EditText)solo.getView(R.id.destination);
        solo.enterText(toAddress, getActivity().getString(R.string.qa_test_address_1));

        EditText amount1 = (EditText)solo.getView(R.id.amount1);
        solo.enterText(amount1, "0.0001");
        solo.clickOnView(solo.getView(R.id.action_send));

        assertTrue("Ensure wallet has sufficient funds!", solo.waitForText(getActivity().getString(R.string.confirm_details),1,500));
        solo.goBack();

    }

    public void testC_InputToAddressFromDropdown() throws AssertionError{

        Spinner mSpinner = solo.getView(Spinner.class, 1);
        int itemCount = mSpinner.getAdapter().getCount();
        View spinnerView = solo.getView(Spinner.class, 1);

        String prevAddress = null;
        for(int i = 0; i < itemCount; i++){

            solo.clickOnView(spinnerView);
            solo.scrollToTop();
            solo.clickOnView(solo.getView(LinearLayout.class, i));
            try{solo.sleep(500);}catch (Exception e){}

            EditText destination = (EditText)solo.getView(R.id.destination);
            String selectedAddress = destination.getText().toString();

            //Test destination address is not empty, and ensure it changes for each account selected
            if(prevAddress!=null)
                assertTrue("Address not populated or address same for different accounts.", !selectedAddress.isEmpty() && !prevAddress.equals(selectedAddress));

            prevAddress = selectedAddress;
        }

    }

    public void testD_InputInvalidAddress() throws AssertionError{

        EditText toAddress = (EditText)solo.getView(R.id.destination);
        solo.enterText(toAddress, "aaaaaaaaaaaaaaaaaaaa");

        EditText amount1 = (EditText)solo.getView(R.id.amount1);
        solo.enterText(amount1, "0.0001");
        solo.clickOnView(solo.getView(R.id.action_send));

        assertTrue("Ensure wallet has sufficient funds!", solo.waitForText(getActivity().getString(R.string.invalid_bitcoin_address),1,500));
        solo.goBack();

        exitApp();
    }
}
