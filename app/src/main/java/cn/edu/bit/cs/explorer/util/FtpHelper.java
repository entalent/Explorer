package cn.edu.bit.cs.explorer.util;

import android.content.Context;
import android.os.Environment;

import com.gc.materialdesign.views.ProgressBarIndeterminate;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import cn.edu.bit.cs.explorer.R;

/**
 * Created by entalent on 2015/12/11.
 */
public class FtpHelper {

    Context context;

    final ArrayList<BaseUser> users = new ArrayList<>();
    FtpServer server;
    final FtpServerFactory serverFactory;
    ArrayList<FtpServerListener> listeners = new ArrayList<>();
    boolean serverRunnning = false;

    public FtpHelper(Context context) throws IOException {
        this.context = context;
        serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File propFile = getUsersPropertiesFile();
        if(!propFile.exists()) {
            throw new IOException("failed to create property file");
        }
        userManagerFactory.setFile(propFile);
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        listenerFactory.setPort(2221);
        serverFactory.addListener("default", listenerFactory.createListener());
    }

    public File getUsersPropertiesFile() {
        File dataDir = context.getFilesDir();
        File propFile = new File(dataDir.getAbsolutePath() + File.separator + "users.properties");
        InputStream fin = null;
        FileOutputStream fos=null;
        if(!propFile.exists()) {
            try {
                fin = (context.getResources().openRawResource(R.raw.users));
                fos = new FileOutputStream(propFile);
                byte[] buffer = new byte[1024];
                int length = 0;
                while( (length = fin.read(buffer)) != -1){
                    fos.write(buffer,0,length);
                }
            } catch (Exception e){
                System.out.println(dataDir.getAbsolutePath());
                e.printStackTrace(System.out);
            } finally{
                if(fin!=null){
                    try {
                        fin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(fos!=null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return propFile;
    }

    public FtpHelper addUsers(Collection<BaseUser> users) {
        this.users.addAll(users);
        return this;
    }

    public FtpHelper setConnectionConfig(ConnectionConfig config) {
        serverFactory.setConnectionConfig(config);
        return this;
    }

    public boolean startServer() {

        server = serverFactory.createServer();

        if(server.isStopped()) {
            try {
                server.start();
                for(FtpServerListener i : listeners) {
                    i.onServerStartFinish(true);
                }
                serverRunnning = true;
                return true;
            } catch (FtpException e) {
                e.printStackTrace();
                for(FtpServerListener i : listeners) {
                    i.onServerStartFinish(false);
                }
                serverRunnning = false;
                return false;
            }
        }
        return true;
    }

    public void stopServer() {
        if(server == null || server.isStopped()) {
            return;
        }
        server.stop();
        serverRunnning = false;
        for(FtpServerListener i : listeners) {
            i.onServerStop();
        }
    }

    public boolean addListener(FtpServerListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(FtpServerListener listener) {
        return listeners.remove(listener);
    }

    public interface FtpServerListener {
        void onServerStartFinish(boolean running);
        void onServerStop();
    }

    public boolean isServerRunning() {
        return serverRunnning;
    }
}
