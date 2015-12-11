package cn.edu.bit.cs.explorer.util;

import com.gc.materialdesign.views.ProgressBarIndeterminate;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by entalent on 2015/12/11.
 */
public class FtpHelper {

    final ArrayList<BaseUser> users = new ArrayList<>();
    FtpServer server;
    final FtpServerFactory serverFactory;
    ArrayList<FtpServerListener> listeners = new ArrayList<>();
    boolean serverRunnning = false;

    public FtpHelper() {

        serverFactory = new FtpServerFactory();
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
