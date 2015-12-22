package cn.edu.bit.cs.explorer.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by entalent on 2015/12/5.
 */
public class AdbUtil {
    public static int testRootAccess() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
        dataOutputStream.writeBytes("\n");
        dataOutputStream.flush();
        dataOutputStream.writeBytes("exit\n");
        dataOutputStream.flush();
        process.waitFor();
        return process.exitValue();
    }

    public static void startAdbd(int port) throws IOException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
        dataOutputStream.writeBytes("setprop service.adb.tcp.port " + port + "\n");
        dataOutputStream.flush();
        dataOutputStream.writeBytes("stop adbd\n");
        dataOutputStream.flush();
        dataOutputStream.writeBytes("start adbd\n");
        dataOutputStream.flush();
        dataOutputStream.writeBytes("exit\n");
        dataOutputStream.flush();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        process.destroy();
    }

    public static String getAdbPort() throws IOException {
        Process process = Runtime.getRuntime().exec("getprop service.adb.tcp.port");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }
}
