package com.ident.validator.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ident.ValidateListener;
import com.ident.ValidateResult;
import com.ident.Validator;
import com.ident.validator.core.R;
import com.ident.validator.core.fragment.FailedFragment;
import com.ident.validator.core.fragment.ResultFragment;
import com.ident.validator.core.model.TagInfo;
import com.ident.validator.core.model.TagMessage;
import com.ident.validator.core.utils.NAFVerifyHelper;
import com.ident.validator.core.views.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * @author cheny
 * @version 1.0
 * @descr
 * @date 2017/7/10 11:05
 */

public class ValidatorPresenter implements ValidatorContract.Presenter, ValidateListener {
    static {
        System.loadLibrary("IdentValidator");
    }

    private final ValidatorContract.View mView;
    //    private NAFNfc mNafNfc;
    private Activity mAct;
    private boolean isFirst;
    private ProgressDialog mProgressDialog;
    private TagInfo tagInfo;


    /** 保存第一次读取的证书*/
    private String license = "";

    private String result="";   //保存uid
    private String temp=""; //保存状态位

    /** 测试数据 */
    private String license_test = "";
    private String uid_test = "";
    private String num_test = "";
    private String logo_test = "";
    private String group_test = "";

    /** 双证验证的次数*/
    private int a = 0;

    /** 发送成功的字段*/
    private static final int UPDATE = 1;
    private static final int ADD = 2;

    /** 保存服务器返回数据*/
    private String data_zouyun;

    /** 验证成功才可以进行再次加密*/
    private boolean write_again = false;
    private Activity_zouyun zouyun1 = new Activity_zouyun();

    /** 保存再次写入的证书*/
    private  String license_again = "";
    private String aa = "";

    /** 再次写入是否成功*/
    private boolean isSuccessfullyWritted = false;


    /** handler方法处理网络回复信息*/
   private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE:
                    System.out.println("发送请求成功");
                    /** 为真的一组数据*/
//                    license_test = "32333039";
//                    uid_test = "04edb94ade3680";
//                    num_test = "11111";
//                    logo_test = "五粮液";
//                    System.out.println("Result: " + result + "  " + "license: " + license + "   " + "temp: " + num_test);

                    /** 商品已开启的一组数据*/
//                    license_test = "32333039";
//                    uid_test = "04edb94ade3680";
//                    num_test = "11110";
//                    logo_test = "五粮液";
//                    System.out.println("Result: " + result + "  " + "license: " + license + "   " + "temp: " + num_test);

                    /** 商品为假的一组数据*/
//                    license_test = "32333038";
//                    uid_test = "04edb94ade3680";
//                    num_test = "11111";
//                    logo_test = "五粮液";
//                    System.out.println("Result: " + result + "  " + "license: " + license + "   " + "temp: " + num_test);

                    /** 双标签的验证数据一*/
//                    license_test = "31313931";
//                    uid_test = "040ce50a685f81";
//                    num_test = "01101";
//                    logo_test = "五粮液";
//                    group_test = "11112222";
//                    System.out.println("Result: " + result + "  " + "license: " + license + "   " + "temp: " + num_test);

                    /** 双标签的验证数据二*/
//                    license_test = "30303437";
//                    uid_test = "0406e40a685f81";
//                    num_test = "";
//                    logo_test = "五粮液";
//                    group_test = "11112222";
//                    System.out.println("Result: " + result + "  " + "license: " + license + "   " + "temp: " + num_test);

                    /** 解析数据*/
                    data_zouyun = (String) msg.obj;
                    System.out.println("回复数据为：" + data_zouyun);
                    handleData(data_zouyun);
                    System.out.println("uid_test: " + uid_test + "  " + "license_test: " + license_test
                            + "   " + "num_test: " + num_test + "   " + "group_test: " + group_test
                            + " " + "logo_test: " + logo_test);
                    if(result.equals("")){
                        String color_zouyun = "aaaaa";
                        ResultFragment instance = ResultFragment.newInstance();
                        mView.switchFragment(instance);
                        setMove(color_zouyun,instance);
                        instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                        instance.setResultProduct(false);
                        instance.setTvInfo("未探测到标签");
                    }else {

                        /** 展示结果*/
                        if (temp.equals("")) {
                            System.out.println("这不是RAS标签");
                            String color_zouyun = "aaaaa";
                            ResultFragment instance = ResultFragment.newInstance();
                            mView.switchFragment(instance);
                            setMove(color_zouyun, instance);
                            instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                            instance.setResultProduct(false);
                            instance.setTvInfo("这不是RAS标签");
                        } else {
                            if (group_test.equals("-1")) {
                                if (result.equals(uid_test) && license.equals(license_test)) {
                                    if (temp.equals(num_test)) {
                                        System.out.println("单证商品为真");
                                        setBackground1(logo_test);

                                        //write_again = true;
                                        //saveTagData();

                                        /** 再次写入*/


                                        send2WithOkHttp();

                                    } else {
                                        System.out.println("单证商品已开启");
                                        setBackground3(logo_test);

                                        //write_again = true;
                                        send2WithOkHttp();
                                        //saveTagData();
                                    }
                                } else {
                                    System.out.println("result：" + result + "   " + "uid_test：" + uid_test + "  "
                                                + "license：" + license + "    " + "license_test：" + license_test);
                                    System.out.println("单证商品为假");
                                    setBackground2(logo_test);
                                }
                            } else {
                                if (result.equals(uid_test) && license.equals(license_test)) {
                                    if (a != 1) {
                                        if (temp.equals(num_test)) {
                                            System.out.println("这是双标签，请扫描第二张标签");
                                            System.out.println("组号为：" + group_test);
                                            SharedPreferences pref = mAct.getSharedPreferences("group", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("NUM", group_test);
                                            editor.putString("UID", result);
                                            editor.apply();
                                            a = 1;

                                            setDoubleBackground1(logo_test);

                                            send2WithOkHttp();
                                            //write_again = true;
                                            //saveTagData();
                                        } else {
                                            System.out.println("这是双标签，请扫描第二张标签");
                                            SharedPreferences pref = mAct.getSharedPreferences("group", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("NUM", group_test);
                                            editor.putString("UID", result);
                                            editor.apply();
                                            a = 1;

                                            setDoubleBackground2(logo_test);
                                            send2WithOkHttp();

                                            //write_again = true;
                                            //saveTagData();
                                        }
                                    } else {
                                        SharedPreferences pref = mAct.getSharedPreferences("group", Context.MODE_PRIVATE);
                                        String groupNum = pref.getString("NUM", "1");
                                        String uid_again = pref.getString("UID","");
                                        if (group_test.equals(groupNum) && !(uid_again.equals(result))) {
                                            System.out.println("双证商品为真");
                                            a = 0;

                                            setDoubleBackground3(logo_test);
                                            send2WithOkHttp();

                                            System.out.println("uid_again: " + uid_again);
                                            System.out.println("result: " + result);

                                            //write_again = true;
                                            //saveTagData();
                                        }else if(group_test.equals(groupNum) && uid_again.equals(result)){
                                            System.out.println("这是双标签，请勿重复扫描同一张标签");
                                            ResultFragment instance = ResultFragment.newInstance();
                                            mView.switchFragment(instance);
                                            setMove(temp,instance);
                                            instance.setResultImg1();
                                            instance.setResultProduct(false);
                                            instance.setTvInfo("这是双标签，请勿重复扫描同一张标签");

                                            send2WithOkHttp();
                                        }else {
                                            System.out.println("双证商品为假");
                                            a = 0;

                                            setDoubleBackground4(logo_test);
                                        }
                                    }
                                } else {
                                    System.out.println("商品为假");

                                    setBackground2(logo_test);

                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            ImageView btn_zouyun = (ImageView) mAct.findViewById(R.id.seal_iv);
            btn_zouyun.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(write_again){
                        Intent intent = new Intent(mAct, Activity_zouyun.class);
                        mAct.startActivity(intent);
                    }else{

                    }
                }
            });
        }
    };

    private Handler handler1 = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 2:
                    System.out.println("接收第二个回复成功");
            }
        }
    };





    public ValidatorPresenter(ValidatorContract.View view) {
        this.mView = view;
        mAct = (Activity) mView;
//        mNafNfc.init(mAct);
        Validator.getInstance().init(mAct.getApplicationContext());
        isFirst = true;
    }

    @Override
    public void onStart() {
        if (isFirst) {
            analysisTag();
            isFirst = false;
        }
    }

    @Override
    public void onResume() {
//        mNafNfc.enableForegroundDispatch();
    }

    @Override
    public void onPause() {
//        mNafNfc.disableForegroundDispatch();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onNewIntent(Intent intent) {
        analysisTag();
    }

    @Override
    public void onDestroy() {
//        mNafNfc = null;
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void analysisTag() {
        write_again = false;
        license_again = "";
        aa = "";

        Intent intent = mAct.getIntent();
        Tag tag = NAFVerifyHelper.getNfcData(intent);
        license = "";
        if (tag != null) {
            mView.restUI();

            /** 得到发给服务器的证书号*/
            if(!readMu(tag).equals("")){
                for(int i = 0; i < 4; i++){
                    char a = readMu(tag).charAt(i);
                    String mstr = "3" + a;
                    license = license + mstr;
                }
            }
            System.out.println("证书为：" + license);


            MifareUltralight mifare = MifareUltralight.get(tag);
            result="";
            temp="";


//            license = readMu(tag);
//            System.out.println("证书为：" + license);



            try {
                mifare.connect();
                byte[] payload = mifare.readPages(0);
                for(int j =0;j<8;j++) {
                    temp = Integer.toHexString(payload[j] & 0xFF);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result += temp;
                }
                if(!result.equals("")){
                    result = getuid(result);
                }

                temp = "";
                int move = 0x80;
                byte[] transceive = mifare.transceive(new byte[]{0x30, -128});
                for(int i=0;i<5;i++){
                    if((transceive[0] & move) == 0) temp+="0";
                    else temp+="1";
                    move = move >> 1;
                }
            } catch (IOException e) {
//                boolean isContains = Arrays.asList(ArrayClass_zouyun.UIDARRAY).contains(result);
                boolean isContains = false;

                for(int i = 0; i < ArrayClass_zouyun.UIDARRAY.length; i++){
                    if(result.equals(ArrayClass_zouyun.UIDARRAY[i])){
                        isContains = true;
                        break;
                    }else{

                    }
                }


                int postion = 0;
                int uidCase = 0;
                if (isContains) {
                    for (int i = 0;i < ArrayClass_zouyun.UIDARRAY.length;i++) {
                        if (ArrayClass_zouyun.UIDARRAY[i].equals(result)) {
                            postion = i + 1;
                        }
                    }
                    uidCase = postion % 32;
                    switch (uidCase) {
                        case 0:
                            temp = "00000";
                            break;
                        case 1:
                            temp = "00001";
                            break;
                        case 2:
                            temp = "00010";
                            break;
                        case 3:
                            temp = "00011";
                            break;
                        case 4:
                            temp = "00100";
                            break;
                        case 5:
                            temp = "00101";
                            break;
                        case 6:
                            temp = "00110";
                            break;
                        case 7:
                            temp = "00111";
                            break;
                        case 8:
                            temp = "01000";
                            break;
                        case 9:
                            temp = "01001";
                            break;
                        case 10:
                            temp = "01010";
                            break;
                        case 11:
                            temp = "01011";
                            break;
                        case 12:
                            temp = "01100";
                            break;
                        case 13:
                            temp = "01101";
                            break;
                        case 14:
                            temp = "01110";
                            break;
                        case 15:
                            temp = "01111";
                            break;
                        case 16:
                            temp = "10000";
                            break;
                        case 17:
                            temp = "10001";
                            break;
                        case 18:
                            temp = "10010";
                            break;
                        case 19:
                            temp = "10011";
                            break;
                        case 20:
                            temp = "10100";
                            break;
                        case 21:
                            temp = "10101";
                            break;
                        case 22:
                            temp = "10110";
                            break;
                        case 23:
                            temp = "10111";
                            break;
                        case 24:
                            temp = "11000";
                            break;
                        case 25:
                            temp = "11001";
                            break;
                        case 26:
                            temp = "11010";
                            break;
                        case 27:
                            temp = "11011";
                            break;
                        case 28:
                            temp = "11100";
                            break;
                        case 29:
                            temp = "11101";
                            break;
                        case 30:
                            temp = "11110";
                            break;
                        case 31:
                            temp = "11111";
                            break;
                        default:
                            break;
                    }
                } else {
                    temp = "";
                }
            }
            System.out.println("uid为：" + result);
            System.out.println("状态位为：" + temp);
//            sendWithOkHttp();
            if(!(result.equals("") || license.equals(""))){
                sendWithOkHttp();
            }else{

            }

            getlicense_again();

            writeTag1(tag, aa, mifare);

        }


    }

    public void setMove(String move,ResultFragment instance){
        for(int i=0;i<5;i++){
            Log.d(TAG,move);
            instance.setImageColor(move.charAt(i),i);
        }
    }

    //开始验证
    private void startVerify(Intent intent) {
        mView.setTipViews();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        try {
            Validator.getInstance().validate(tag, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示对应产品信息
    private void showProductFromDisk(String path) {
        String pid = path.substring(path.lastIndexOf("/") + 1, path.length());
        Resources res = mAct.getResources();
        String product_bg = res.getString(R.string.disk_product_bg, path, pid);
        String product_logo = res.getString(R.string.disk_product_logo, path, pid);
        String product_seal = res.getString(R.string.disk_product_seal, path, pid);
        String product_failure = res.getString(R.string.disk_product_failure, path, pid);
        String product_success = res.getString(R.string.disk_product_success, path, pid);
        String product_img = res.getString(R.string.disk_product_img, path, pid);
        Log.e("TAG", "product_bg:" + product_bg);
    }

    //显示对应产品信息
    private boolean showProduct(String pid) {
        if (TextUtils.equals(pid, "010001000200040001")) {
            pid = "010001000100000031";
        }
        if (TextUtils.equals(pid, "0700ff00ff00ff0001")) {
            pid = "070001000100ff0001";
        }
        Resources res = mAct.getResources();
        int product_bg = getDrawableId(R.string.product_bg, pid, res);
        int product_logo = getDrawableId(R.string.product_logo, pid, res);
        int product_seal = getDrawableId(R.string.product_seal, pid, res);
        int product_failure = getDrawableId(R.string.product_failure, pid, res);
        int product_success = getDrawableId(R.string.product_success, pid, res);
        int product_img = getDrawableId(R.string.product_img, pid, res);
//        if (product_bg == 0 || product_logo == 0 || product_seal == 0 ||
//                product_failure == 0 || product_success == 0 || product_img == 0) {
//            return false;
//        }
        if (product_logo == 0 || product_failure == 0 || product_success == 0) {
            return false;
        }
        // TODO: 2017/7/20  待替换为070001000100ff0001//云艺术pid d1560001391b0700ff00ff00ff000100
        if (TextUtils.equals(pid, "070001000100ff0001") || TextUtils.equals(pid, "0700ff00ff00ff0001")) {
            mView.switchFragment(CloudArtFragment.newInstance());
        } else {
            mView.switchFragment(ValidatorFragment.newInstance());
        }
        mView.showProduct(product_bg, product_logo, product_seal, product_failure, product_success, product_img);
        return true;
    }

    private int getDrawableId(int sId, String pid, Resources resources) {
        return resources.getIdentifier(mAct.getString(sId, pid), "mipmap", mAct.getPackageName());
    }

    //解析标签信息
    private TagInfo parseProductTag(Intent intent) {
        TagInfo tagInfo = new TagInfo();
        if (intent != null) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null) {
                String[] schemes = new String[]{"d156000139", "d156000189"};
                for (String scheme : schemes) {
                    tagInfo = parseTaInfo(parcelables, scheme.getBytes());//tagInfo可以为null
                    if (tagInfo != null)
                        break;
                }
            }
            //tagInfo.tid = NAFVerifyHelper.getTagTid(NAFVerifyHelper.getNfcData(intent));
        }
        //Log.e("ident", "tagInfo:" + tagInfo.toString());
        return tagInfo;
    }

    private TagInfo parseTaInfo(Parcelable[] parcelables, byte[] scheme) {
        TagInfo tagInfo = null;
//        inner:
        for (int i = 0; i < parcelables.length; i++) {
            NdefMessage msg = (NdefMessage) parcelables[i];
            NdefRecord[] records = msg.getRecords();
            for (int j = 0; j < records.length; j++) {
                byte[] payload = records[j].getPayload();
                Log.d(TAG, "parseTaInfo: "+bytesToHexString(scheme));
                Log.d(TAG, "parseTaInfo: "+bytesToHexString(payload));
                if (payload != null && payload.length > scheme.length) {
                    boolean found = true;
                    for (int k = 0; k < scheme.length; ++k) {
                        if (scheme[k] != payload[k + 1]) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        int offset = scheme.length + 1 + 3 + 1;
//                        int type = payload[offset];
//                        int sub1 = (payload[offset + 1] << 8) | payload[offset + 2];
//                        int sub2 = (payload[offset + 3] << 8) | payload[offset + 4];
                        int vendor = ((payload[offset + 5] << 8) & 0XFF) | (payload[offset + 6] & 0XFF);
//                        int brand = (payload[offset + 7] << 8) | payload[offset + 8];
                        tagInfo = new TagInfo();
                        tagInfo.vendor = vendor;

                        byte[] desAry = new byte[9];
                        System.arraycopy(payload, offset, desAry, 0, 9);
                        tagInfo.pid = NAFVerifyHelper.bytesToHexString(desAry);

                        byte[] aid = new byte[24];
                        System.arraycopy(payload, j + 1, aid, 0, 24);
                        tagInfo.aid = NAFVerifyHelper.bytesToHexString(NAFVerifyHelper.parse(aid));
                    }
                }
            }
        }
        return tagInfo;
    }

    @Override
    public void onError(Validator.ErrorCode errorCode) {
        mView.showAlert(errorCode.toString());
        mView.restUI();
        Log.e("ident", "onError:" + errorCode);
    }

    @Override
    public void onResult(ValidateResult validateResult) {
        Log.e("ident", "onResult url:" + validateResult.url() + "--code:" + validateResult.code() + "--isValid:" + validateResult.isValid() + "--aid:" + NAFVerifyHelper.bytesToHexString(validateResult.aid()));
        if (validateResult != null) {
            if (validateResult.isValid()) {
                //d1560001391b0700ff00ff00ff000100
                //d1560001391b070001000100ff000100
                if (TextUtils.equals("d1560001391b01000100020004000100", NAFVerifyHelper.bytesToHexString(validateResult.aid())) ||
                        TextUtils.equals("d1560001391b070001000100ff000100", NAFVerifyHelper.bytesToHexString(validateResult.aid())) ||
                        TextUtils.equals("d1560001391b0700ff00ff00ff000100", NAFVerifyHelper.bytesToHexString(validateResult.aid()))) {
                    mView.jump2Result(validateResult.url());
                } else {
                    mView.showSuccess(tagInfo.isTest());
                }
            } else {
                mView.showFailure(tagInfo.isTest());
            }
        }
    }


    @Override
    public void onProgress(int i) {
        Log.e("ident", "onProgress:" + i);
    }

    private TagRequest mTagRequest;

    @Override
    public void onMoreTagRequest(TagRequest tagRequest) {
        mTagRequest = tagRequest;
        Log.e("ident", "onMoreTagRequest:" + tagRequest.toString());
    }

    @Override
    public void onLogMessage(String s) {
        Log.e("ident", "onLogMessage:" + s);
    }

    @Override
    public void showToolbarRightMenu() {
        mView.showToolbarRightMenu(getToolbarRightJumpUrl(), TextUtils.isEmpty(getToolbarRightJumpUrl()));
    }

    @Override
    public String getToolbarRightJumpUrl() {
        if (tagInfo != null) {
            return "http://pdp.ident.cn/ProductPortal/Product/WebSite?aid=" + tagInfo.aid.toUpperCase() + "?tid=" + tagInfo.tid.toUpperCase();
        }
        return null;
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if(src != null && src.length > 0) {
            for(int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if(hv.length() < 2) {
                    builder.append(0);
                }

                builder.append(hv);
            }

            return builder.toString();
        } else {
            return null;
        }
    }

    /**
     * 读取证书22页
     */
    public String readMu(Tag tag){
        MifareUltralight mifare_zouyun = MifareUltralight.get(tag);
        String a = "";
        String b = "";
        try {
            mifare_zouyun.connect();
            byte[] data = mifare_zouyun.readPages(22);
            a = new String(data, Charset.forName("US-ASCII"));
            mifare_zouyun.close();
            b = getCer(a);
        } catch (IOException e) {
            Toast.makeText(mAct, "探测标签失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 获取实际的四位随机数证书
     */
    public String getCer(String a){
        char a1 = a.charAt(0);
        char a2 = a.charAt(1);
        char a3 = a.charAt(2);
        char a4 = a.charAt(3);
        String b = "" + a1 + a2 + a3 + a4;
        return b;
    }
    /**
     * uid转换
     */
    public String getuid(String result){
        String uid_zouyun = "";
        for(int i = 0; i < 6; i++){
            char a = result.charAt(i);
            uid_zouyun = uid_zouyun + a;
        }
        for (int i = 8; i < 16; i++){
            char a = result.charAt(i);
            uid_zouyun = uid_zouyun + a;
        }
        return uid_zouyun;
    }

    /**
     * 发送第一次网络请求
     */
    public void sendWithOkHttp(){
        Thread a1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody;
                    if(temp != "" && license != ""){
                         requestBody = new FormBody.Builder()
                                .add("uid", result)
                                .add("certificate", license)
                                .add("obflag", temp)
                                .build();
                    }else if(license != "" && temp == ""){
                        requestBody = new FormBody.Builder()
                                .add("uid", result)
                                .add("certificate", license)
                                .build();
                    }else{
                        requestBody = new FormBody.Builder()
                                .add("uid", result)
                                .add("obflag", temp)
                                .build();
                    }

                    Request request = new Request.Builder().url("http://47.96.123.242/validate").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    if(response.isSuccessful()){
                        Message msg = new Message();
                        msg.what = UPDATE;
                        msg.obj = responseData;
                        handler.sendMessage(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        a1.start();
        try {
            a1.join();
            System.out.println("a1.join()成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("a1.join()异常");
        }
    }

    /**
     * 解析json数据
     */
    private void handleData(String responsedata){
        try {
            JSONObject object1 = new JSONObject(responsedata);
            JSONObject object2 = object1.getJSONObject("taginfo");
            uid_test = object2.getString("uid");
            license_test = object2.getString("certificate");
            num_test = object2.getString("obflag");
            logo_test = object2.getString("brand");
            group_test = object2.getString("group_number");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置单标签为真结果图片
     */
    public void setBackground1(String logo){
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp,instance);
        switch(logo){
            case "五粮春":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
                instance.setResultImg(R.mipmap.p_010001000100000041_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
                instance.setResultImg(R.mipmap.p_010001000100000011_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
                instance.setResultImg(R.mipmap.p_010001000100000021_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
                instance.setResultImg(R.mipmap.p_010001000100000031_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
                instance.setResultImg(R.mipmap.p_010001000100000051_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
                instance.setResultImg(R.mipmap.p_010001000100000061_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
                instance.setResultImg(R.mipmap.p_010001000100000071_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
                instance.setResultImg(R.mipmap.p_020001000100000091_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
                instance.setResultImg(R.mipmap.p_020001000100000092_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
                instance.setResultImg(R.mipmap.p_020001000100000093_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
                instance.setResultImg(R.mipmap.p_020001000100000094_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
                instance.setResultImg(R.mipmap.p_060001000100000081_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.zouyun1);
                break;
            default:
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    /**
     *  设置单标签为假结果图片
     */
    public void setBackground2(String logo){
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp,instance);
        switch(logo){
            case "五粮春":
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
                instance.setResultImg(R.mipmap.p_010001000100000041_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
                instance.setResultImg(R.mipmap.p_010001000100000011_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
                instance.setResultImg(R.mipmap.p_010001000100000021_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
                instance.setResultImg(R.mipmap.p_010001000100000031_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
                instance.setResultImg(R.mipmap.p_010001000100000051_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
                instance.setResultImg(R.mipmap.p_010001000100000061_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
                instance.setResultImg(R.mipmap.p_010001000100000071_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
                instance.setResultImg(R.mipmap.p_020001000100000091_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
                instance.setResultImg(R.mipmap.p_020001000100000092_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
                instance.setResultImg(R.mipmap.p_020001000100000093_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
                instance.setResultImg(R.mipmap.p_020001000100000094_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
                instance.setResultImg(R.mipmap.p_060001000100000081_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.zouyun1);
                break;
            default:
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    /**
     *  设置单标签状态位改变结果图片
     */
    public void setBackground3(String logo) {
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp, instance);
        switch (logo) {
            case "五粮春":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
                instance.setResultImg(R.mipmap.p_010001000100000041_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
                instance.setResultImg(R.mipmap.p_010001000100000011_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
                instance.setResultImg(R.mipmap.p_010001000100000021_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
                instance.setResultImg(R.mipmap.p_010001000100000031_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
                instance.setResultImg(R.mipmap.p_010001000100000051_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
                instance.setResultImg(R.mipmap.p_010001000100000061_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
                instance.setResultImg(R.mipmap.p_010001000100000071_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
                instance.setResultImg(R.mipmap.p_020001000100000091_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
                instance.setResultImg(R.mipmap.p_020001000100000092_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
                instance.setResultImg(R.mipmap.p_020001000100000093_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
                instance.setResultImg(R.mipmap.p_020001000100000094_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
                instance.setResultImg(R.mipmap.p_060001000100000081_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.zouyun1);
                break;

            default:
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品已开启, 品牌为" + logo_test);
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }


    /**
     * 设置双标签第一张标签为真结果图片
     */
    public void setDoubleBackground1(String logo) {
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp, instance);
        instance.setResultImg1();
        instance.setResultProduct(false);
        switch (logo) {
            case "五粮春":
//                instance.setResultImg(R.mipmap.p_010001000100000002_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
//                instance.setResultImg(R.mipmap.p_010001000100000041_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
//                instance.setResultImg(R.mipmap.p_010001000100000011_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
//                instance.setResultImg(R.mipmap.p_010001000100000021_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
//                instance.setResultImg(R.mipmap.p_010001000100000031_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
//                instance.setResultImg(R.mipmap.p_010001000100000051_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
//                instance.setResultImg(R.mipmap.p_010001000100000061_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
//                instance.setResultImg(R.mipmap.p_010001000100000071_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
//                instance.setResultImg(R.mipmap.p_020001000100000091_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
//                instance.setResultImg(R.mipmap.p_020001000100000092_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
//                instance.setResultImg(R.mipmap.p_020001000100000093_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
//                instance.setResultImg(R.mipmap.p_020001000100000094_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
//                instance.setResultImg(R.mipmap.p_060001000100000081_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
//                instance.setResultImg(R.mipmap.p_060001000100000081_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            default:
//                instance.setResultImg(R.mipmap.p_010001000100000002_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    /**
     * 设置第一张标签状态位改变结果图片
     */
    public void setDoubleBackground2(String logo) {
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp, instance);
        instance.setResultImg1();
        instance.setResultProduct(false);
        switch (logo) {
            case "五粮春":
//                instance.setResultImg(R.mipmap.p_010001000100000002_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
//                instance.setResultImg(R.mipmap.p_010001000100000041_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
//                instance.setResultImg(R.mipmap.p_010001000100000011_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
//                instance.setResultImg(R.mipmap.p_010001000100000021_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
//                instance.setResultImg(R.mipmap.p_010001000100000031_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
//                instance.setResultImg(R.mipmap.p_010001000100000051_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
//                instance.setResultImg(R.mipmap.p_010001000100000061_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
//                instance.setResultImg(R.mipmap.p_010001000100000071_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
//                instance.setResultImg(R.mipmap.p_020001000100000091_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
//                instance.setResultImg(R.mipmap.p_020001000100000092_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
//                instance.setResultImg(R.mipmap.p_020001000100000093_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
//                instance.setResultImg(R.mipmap.p_020001000100000094_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
//                instance.setResultImg(R.mipmap.p_060001000100000081_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
//                instance.setResultImg(R.mipmap.p_060001000100000081_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            default:
//                instance.setResultImg(R.mipmap.p_010001000100000002_success);
//                instance.setResultProduct(true);
                instance.setTvInfo("这是双标签，请扫描第二张标签");
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    /**
     * 设置第二张标签为真结果图片
     */
    public void setDoubleBackground3(String logo) {
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp, instance);
        switch (logo) {
            case "五粮春":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
                instance.setResultImg(R.mipmap.p_010001000100000041_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
                instance.setResultImg(R.mipmap.p_010001000100000011_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
                instance.setResultImg(R.mipmap.p_010001000100000021_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
                instance.setResultImg(R.mipmap.p_010001000100000031_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
                instance.setResultImg(R.mipmap.p_010001000100000051_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
                instance.setResultImg(R.mipmap.p_010001000100000061_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
                instance.setResultImg(R.mipmap.p_010001000100000071_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
                instance.setResultImg(R.mipmap.p_020001000100000091_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
                instance.setResultImg(R.mipmap.p_020001000100000092_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
                instance.setResultImg(R.mipmap.p_020001000100000093_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
                instance.setResultImg(R.mipmap.p_020001000100000094_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
                instance.setResultImg(R.mipmap.p_060001000100000081_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.zouyun1);
                break;
            default:
                instance.setResultImg(R.mipmap.p_010001000100000002_success);
                instance.setResultProduct(true);
                instance.setTvInfo("商品为真, 品牌为" + logo_test);
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    /**
     * 设置第二张标签不匹配结果图片
     */
    public void setDoubleBackground4(String logo){
        ResultFragment instance = ResultFragment.newInstance();
        mView.switchFragment(instance);
        setMove(temp,instance);
        switch(logo){
            case "五粮春":
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
            case "剑南春":
                instance.setResultImg(R.mipmap.p_010001000100000041_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000041_img);
                break;
            case "洋河酒":
                instance.setResultImg(R.mipmap.p_010001000100000011_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000011_img);
                break;
            case "泸州老窖":
                instance.setResultImg(R.mipmap.p_010001000100000021_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000021_img);
                break;
            case "茅台酒":
                instance.setResultImg(R.mipmap.p_010001000100000031_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000031_img);
                break;
            case "古井贡酒":
                instance.setResultImg(R.mipmap.p_010001000100000051_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000051_img);
                break;
            case "杜康酒":
                instance.setResultImg(R.mipmap.p_010001000100000061_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000061_img);
                break;
            case "郎酒":
                instance.setResultImg(R.mipmap.p_010001000100000071_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_010001000100000071_img);
                break;
            case "玉溪烟":
                instance.setResultImg(R.mipmap.p_020001000100000091_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000091_img);
                break;
            case "黄鹤楼烟":
                instance.setResultImg(R.mipmap.p_020001000100000092_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000092_img);
                break;
            case "黄山徽商烟":
                instance.setResultImg(R.mipmap.p_020001000100000093_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000093_img);
                break;
            case "九寨沟烟":
                instance.setResultImg(R.mipmap.p_020001000100000094_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_020001000100000094_img);
                break;
            case "阿玛尼服饰":
                instance.setResultImg(R.mipmap.p_060001000100000081_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.p_060001000100000081_img);
                break;
            case "abcam":
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                instance.setImg_zouyun(R.mipmap.zouyun1);
                break;
            default:
                instance.setResultImg(R.mipmap.p_010001000100000002_failure);
                instance.setResultProduct(false);
                instance.setTvInfo("商品为假, 品牌为" + logo_test);
                //instance.setImg_zouyun(R.mipmap.p_010001000100000002_img);
                break;
        }
    }

    public void saveTagData() {
        SharedPreferences preferences = mAct.getSharedPreferences("info",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("brand",logo_test);
        editor.putString("group_number",group_test);
        editor.apply();
    }



    public void send2WithOkHttp(){
        Thread a1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody;
                    requestBody = new FormBody.Builder().add("uid", result).add("certificate", license_again).build();

                    Request request = new Request.Builder().url("http://47.96.123.242/function/add_tag").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    if(response.isSuccessful()){
                        Message msg1 = new Message();
                        msg1.what = ADD;
                        msg1.obj = responseData;
                        handler1.sendMessage(msg1);
                        System.out.println("第二个网络请求成功");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        a1.start();

    }

    /**
     * 产生再次写入的证书
     */
    public void getlicense_again(){
        int a0, a1, a2, a3;
        a0 = (int) (Math.random() * 10);
        a1 = (int) (Math.random() * 10);
        a2 = (int) (Math.random() * 10);
        a3 = (int) (Math.random() * 10);
        aa = Integer.toString(a0) + Integer.toString(a1) + Integer.toString(a2) + Integer.toString(a3);
        for(int i = 0; i < 4; i++){
            char a = aa.charAt(i);
            String mstr = "3" + a;
            license_again = license_again + mstr;
        }
        System.out.println("再次写入的证书为：" + license_again);
    }

    /**
     * 再次写入证书的操作
     */
    public void write_again(String a){
        a = license_again;
        if (a == null)
            return;
        //获取Tag对象
        Intent intent = mAct.getIntent();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);



        //写非NDEF格式的数据
        String[] techList = tag.getTechList();
        boolean haveMifareUltralight = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareUltralight") >= 0) {
                haveMifareUltralight = true;
                break;
            }
        }

        if (!haveMifareUltralight) {
            Toast.makeText(mAct, "不支持MifareUltralight数据格式", Toast.LENGTH_SHORT).show();
            return;
        }
        //writeTag1(tag, a);
    }

    private void writeTag1(Tag tag, String num, MifareUltralight ultralight) {
        //MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            //ultralight.connect();

            ultralight.writePage(22, num.getBytes(Charset.forName("US-ASCII")));

            System.out.println("非ndef数据写入成功");
            isSuccessfullyWritted = true;
        } catch (Exception e) {
            System.out.println("非ndef写入异常");
            isSuccessfullyWritted = false;
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
            }
        }
    }

}
