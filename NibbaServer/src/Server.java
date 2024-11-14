//sono rimasto al minuto 12:36 quando viene fatto il while che controlla di continuo cosa ha scritto l'utente e se message != null controlla se inizia con qualche "formula" speciale

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>(); //Array con tutte le connessioni ai Client
        done = false; //flag che controlla se il server è ancor aperto
    }

    /*Finchè il servver è aperto, il server accetta la connessione, le associa un ConnectionHandler, aggiunge il ConnectionHandler ad
    un array di cui fanno parte tutte le Connections, lo Handler viene poi eseguito
     */
    @Override
    public void run(){
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            try {
                shutdown();
            } catch (IOException ex) {
                //nel tutorial questo try/catch non c'è, ma a me da errore se non lo metto
            }
        }
    }

    /*broadcast fa un for dove controlla tutte le connections, ovvero tutti gli utenti connessi se
    la connessione != null, allora c'è qualcosa di connesso al quale con ch.sendMessage() sarà mandato il messagio*/
    public void broadcast(String message){
        for (ConnectionHandler ch : connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() throws IOException {
        try{
            done = true;
            pool.shutdown();
            if(!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler ch : connections){
                ch.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }

    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        //Viene runnato il connection handler dopo aver effettuato la connessione, questo e il codice che manda e riceve i messaggi al client associato all'handler
        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(), true); //ciò che viene mandato all'user
                in = new BufferedReader(new InputStreamReader(client.getInputStream())); //ciò che viene mandato dall'user
                out.println("Prego, inserire il proprio nome: ");
                nickname = in.readLine();
                System.out.println(nickname + " si è connesso");
                broadcast(nickname + " e' entrato in chat");
                String message;
                while ((message = in.readLine()) != null){
                    if(message.startsWith("/quit")){
                        broadcast(nickname + " ha abbandonato la chat");
                        shutdown();
                    }
                    else{
                        broadcast(nickname + ": " + message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }

        }

        //sendMessage invia il messaggio al client
        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown(){
            if(!client.isClosed()){
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }
    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }
}
