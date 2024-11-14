import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    private String message;

    @Override
    public void run() {
        try {
            client = new Socket("192.168.1.52", 9999);
            out = new PrintWriter(client.getOutputStream(), true);//Invia sul Socket client
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); //legge ciò che arriva dal Socket client
            InputHandler inHandler = new InputHandler(); //crea un input handler locale
            Thread t = new Thread(inHandler); //crea un thread dell'input handler
            t.start(); //fa partire il thread creato sopra


            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);  //stampa a schermo i messaggi
            }
        }catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
            //ignore
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in)); //serve a leggere ciò che l'utente client sta scrivendo
                while(!done){
                    message = inReader.readLine(); //legge ciò che l'utente ha scritto
                    if (message.equals("/quit")){
                        out.println(message); //manda il messaggio quit al server che chiuderà la connessione
                        inReader.close(); //chiude il lettore di input
                        shutdown();
                    }
                    else{
                        out.println(message); //Invia il messaggio
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }
    }
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }
}
