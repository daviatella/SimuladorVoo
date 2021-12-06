package br.nom.belo.marcio.simuladorvoo;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Aviao implements Runnable {

    private Aeroporto aeroporto;
    private String idAviao;
    private long tempoVoo=0;
    private static final Logger LOG = LoggerFactory.getLogger( "Aviao");
    public Aviao(Aeroporto aeroporto, String idAviao,long tempoVoo) {
        this.aeroporto = aeroporto;
        this.idAviao = idAviao;
        this.tempoVoo=tempoVoo;
    }

    public void run() {   	    	
	    try {
	    	Thread.sleep( tempoVoo / 2);
	    } catch (InterruptedException ie) {   
	        Util.logarEReinterromper();
	    }
    	
        decolar();
        voar();
        aterrisar();
        LOG.info( "{} em solo.", idAviao);
    }

    private synchronized void decolar() {
        LOG.info( "{} pedindo autorização ao {} para decolar...", idAviao, aeroporto.getNomeAeroporto());
        aeroporto.esperarPistaDisponivel(idAviao);
        LOG.info("{} decolando...", idAviao);   
    }

    private void voar() {
        LOG.info( "{}: voando...", idAviao);
        try {
            Thread.sleep( tempoVoo);
        } catch (InterruptedException e) {    
            Util.logarEReinterromper();
        }
    }

    private synchronized void aterrisar() {
       LOG.info( "{} pedindo autorização ao {} para aterrisar...", idAviao, aeroporto.getNomeAeroporto());
       aeroporto.esperarPistaDisponivel(idAviao);
       LOG.info( "{} aterrisando...", idAviao);
    }
}

class Aeroporto implements Runnable {

    private boolean temPistaDisponivel = true;
    private String nomeAeroporto;
    private Random random = new Random();
    private static final Logger LOG = LoggerFactory.getLogger( "Aeroporto");
    //Alterado por Davi v
    private static boolean running = true;
    public Aeroporto(String nomeAeroporto) {
        
        this.nomeAeroporto = nomeAeroporto;
    }

    public String getNomeAeroporto() {
        
        return nomeAeroporto;
    }
    
    public synchronized void esperarPistaDisponivel(String idAviao) { 
        //Alterado por Davi v
	    while(!temPistaDisponivel) {
	    	try {
				this.wait();
			} catch (InterruptedException e) {
                Util.logarEReinterromper();
			}
	    }
	    LOG.info("{} autoriza {} para utilizar a pista", nomeAeroporto, idAviao);
	    temPistaDisponivel = false;
    }

    public synchronized void mudarEstadoPistaDisponivel() {
        
        // Inverte o estado da pista.
        temPistaDisponivel = !temPistaDisponivel;

        LOG.info( "{} tem pista disponível? {}", nomeAeroporto, (temPistaDisponivel ? "Sim" : "Não"));
        // Notifica a mudanca de estado para quem estiver esperando.
       if(temPistaDisponivel) {
          this.notifyAll();
       }
    }
    
    //Alterado por Davi v
    public synchronized void pararVoo() {
    	running = false;
    }
    
    public void run() {

        LOG.info( "Rodando aeroporto {}", nomeAeroporto);
        
        do {
            try {
            	this.mudarEstadoPistaDisponivel();
                // Coloca a thread aeroporto dormindo por um tempo de 0 a 5s
                Thread.sleep( random.nextInt( 5000)); 
            } catch (InterruptedException e) {

                Util.logarEReinterromper();
            }
        //Alterado por Davi v
        } while(running); // NOSONAR
    }
}
/*
 * Simulador de voo com threads
 */
public final class SimuladorVoo {
    
    private static final Logger LOG = LoggerFactory.getLogger( "SimuladorVoo");

    public static void main(String[] args) {

        LOG.info( "Rodando simulador de voo.");

        // Constroi aeroporto e inicia sua execucao.
        // NÃO MEXER NESSE TRECHO
        Aeroporto santosDumont = new Aeroporto( "Santos Dumont");
        Thread threadAeroporto = new Thread( santosDumont, "santosDumont");

        // Constrói aviao e inicia sua execucao.
        // NÃO MEXER NESSE TRECHO
        Aviao aviao14bis = new Aviao( santosDumont, "Avião 14BIS",10000);
        Thread thread14bis = new Thread( aviao14bis, "aviao14bis");
        //Alterado por Davi v
        Aviao aviao747 = new Aviao( santosDumont, "Avião 747",5000);
        Thread thread747 = new Thread( aviao747, "aviao747");
        Aviao aviao363 = new Aviao( santosDumont, "Avião 363",3000);
        Thread thread363 = new Thread( aviao363, "aviao363");
        Aviao aviao220 = new Aviao( santosDumont, "Avião 220",13000);
        Thread thread220 = new Thread( aviao220, "aviao220");
        Aviao aviao969 = new Aviao( santosDumont, "Avião 969",7500);
        Thread thread969 = new Thread( aviao969, "aviao969");
        
        // Inicia as threads
        threadAeroporto.start();
        thread14bis.start();
        //Alterado por Davi v
        thread747.start();
        thread363.start();
        thread220.start();
        thread969.start(); 

        try {
            //Alterado por Davi v
        	 thread14bis.join();
             thread747.join();
             thread363.join();
             thread220.join();
             thread969.join(); 
             santosDumont.pararVoo();
        } catch (InterruptedException ex) {
            
            Util.logarEReinterromper();
        }
        LOG.info( "Terminando thread principal.");
    }
}

class Util {

    private static final Logger LOG = LoggerFactory.getLogger( "Util");

    private Util() { }

    static void logarEReinterromper() {

        LOG.error( "Thread interrompida");
        Thread.currentThread().interrupt();
    }
}