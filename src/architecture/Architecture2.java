package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import components.Bus;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture2 {
	
	private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
								//simulation mode shows the components' status after each instruction
	
	
	private boolean halt;
	private Bus extbus1;
	private Bus intbus1;
	private Memory memory;
	private int memorySize;
	private Register PC;
	private Register IR;
	private Register RPG0;
	private Register RPG1;
	private Register RPG2;
	private Register RPG3;
	private Register Flags;
	private Ula ula;
	private Bus demux; //only for multiple register purposes
	
	private ArrayList<String> commandsList;
	private ArrayList<Register> registersList;
	
	

	/**
	 * Instanciates all components in this architecture
	 */
	
	private void componentsInstances() {
		//don't forget the instantiation order
		//buses -> registers -> ula -> memory
		extbus1 = new Bus();
		intbus1 = new Bus();	
		PC = new Register("PC", extbus1,null);
		IR = new Register("IR", extbus1, intbus1);
		RPG0 = new Register("RPG0",null, intbus1);
		RPG1 = new Register ("RPG1", null, intbus1);
		RPG2 = new Register ("RPG2", null, intbus1);
		RPG3 = new Register ("RPG3", null, intbus1);
		Flags = new Register(2, intbus1);
		fillRegistersList();
		ula = new Ula(intbus1, extbus1);
		memorySize = 128;
		memory = new Memory(memorySize, extbus1);
		demux = new Bus(); //this bus is used only for multiple register operations
		
		fillCommandsList();
	}

	/**
	 * This method fills the registers list inserting into them all the registers we have.
	 * IMPORTANT!
	 * The first register to be inserted must be the default RPG
	 */
	private void fillRegistersList() {		
		registersList = new ArrayList<Register>();
		registersList.add(RPG0);
		registersList.add(RPG1);
		registersList.add(RPG2);
		registersList.add(RPG3);
		registersList.add(PC);
		registersList.add(IR);
		registersList.add(Flags);
	}

	/**
	 * Constructor that instanciates all components according the architecture diagram
	 */
	public Architecture2() {
		componentsInstances();
		
		//by default, the execution method is never simulation mode
		simulation = false;
	}

	
	public Architecture2(boolean sim) {
		componentsInstances();
		
		//in this constructor we can set the simoualtion mode on or off
		simulation = sim;
	}



	//getters
	
	protected Bus getExtbus1() {
		return extbus1;
	}

	protected Bus getIntbus1() {
		return intbus1;
	}

	protected Memory getMemory() {
		return memory;
	}

	protected Register getPC() {
		return PC;
	}

	protected Register getIR() {
		return IR;
	}

	protected Register getRPG0() {
		return RPG0;
	}

	protected Register getRPG1() {
		return RPG1;
	}

	protected Register getRPG2() {
		return RPG2;
	}

	protected Register getRPG3() {
		return RPG3;
	}

	protected Register getFlags() {
		return Flags;
	}

	protected Ula getUla() {
		return ula;
	}

	public ArrayList<String> getCommandsList() {
		return commandsList;
	}



	//all the microprograms must be impemented here
	
	/**
	 * This method fills the commands list arraylist with all commands used in this architecture
	 */
	
	protected void fillCommandsList() {
		commandsList = new ArrayList<String>();
		//Adds
		commandsList.add("addRegReg");   //0
		commandsList.add("addMemReg");   //1
		commandsList.add("addRegMem");   //2
		commandsList.add("addImmReg");   //3
		
		//Subs
		commandsList.add("subRegReg");   //4
		commandsList.add("subMemReg");   //5
		commandsList.add("subRegMem");   //6	
		commandsList.add("subImmReg");   //7
		
		//Imuls
		commandsList.add("imulMemReg");   //8
		commandsList.add("imulRegMem");   //9
		commandsList.add("imulRegReg");   //10
		
		//Moves
		commandsList.add("moveRegReg");   //11
		commandsList.add("moveMemReg");   //12
		commandsList.add("moveRegMem");   //13
		commandsList.add("moveImmReg");   //14
		
		commandsList.add("incReg");   //15
		
		//Desvios
		commandsList.add("jmp");   //16
		commandsList.add("jn");    //17
		commandsList.add("jz");    //18
		commandsList.add("jeq");    //19
		commandsList.add("jneq");    //20
		commandsList.add("jgt");    //21
		commandsList.add("jlw");    //22
				
		//
		commandsList.add("read");  //23
		commandsList.add("store"); //24
		commandsList.add("ldi");   //25	
		
	}

	
	/**
	 * This method is used after some ULA operations, setting the flags bits according the result.
	 * @param result is the result of the operation
	 * NOT TESTED!!!!!!!
	 */
	private void setStatusFlags(int result) {
		Flags.setBit(0, 0);
		Flags.setBit(1, 0);
		if (result==0) { //bit 0 in flags must be 1 in this case
			Flags.setBit(0,1);
		}
		if (result<0) { //bit 1 in flags must be 1 in this case
			Flags.setBit(1,1);
		}
	}
	
	public void incPc() {
		PC.read();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.store(); //now PC points to the parameter address
	}
	
	//ADDS

	
	public void addRegReg() {
		this.incPc();
	    
		//  Ler o ID do primeiro registrador da memória
	    PC.read(); 
	    memory.read();
	    
	    // Armazenar o ID do primeiro registrador no demux e iniciar a leitura do registrador
	    demux.put(extbus1.get()); // Seleciona o registrador correto
	    registersInternalRead(); 
	    ula.store(0);
	    
	    // Atualizar PC para apontar para o segundo parâmetro (ID do segundo registrador)
	    this.incPc();
	    
	    // Ler o ID do segundo registrador da memória
	    PC.read();
	    memory.read();
	    
	    // Armazenar o ID do segundo registrador no demux e iniciar a leitura do registrador
	    demux.put(extbus1.get()); 
	    registersInternalRead();
	    ula.store(1); 
	    
	    // Realizar a operação de adição na ULA
	    ula.add();
	    
	    // Atualizar os flags de status com base no resultado da operação
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar o resultado da adição de volta no registrador de destino
	    ula.internalRead(1);
	    demux.put(extbus1.get());
	    registersInternalStore();
	    
	    this.incPc();
	}
	
	
	
	public void addMemReg() {
	    // Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler valor da memória
	    PC.read(); 
	    memory.read(); 
	    memory.read();
	    ula.store(0); // Operando 1: valor da memória
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read(); 
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get()); 
	    registersInternalRead(); 
	    ula.store(1); // Operando 2: valor do registrador
	    
	    // Realizar adição na ULA
	    ula.add();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1);
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}
	
	public void addRegMem() {
	    // Incrementar PC para o registrador de origem
	    this.incPc();
	    
	    // Ler ID do registrador de origem
	    PC.read(); 
	    memory.read();
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Operando 1: valor do registrador
	    
	    // Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler valor da memória
	    PC.read();
	    memory.read(); 
	    memory.read();
	    ula.store(1); // Operando 2: valor da memória
	    
	    // Realizar adição na ULA
	    ula.add();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado na memória
	    ula.read(1);
	    memory.store(); // Obs.:  como a memoria sabe o endereco para armazenar ?
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}
	
		
	public void addImmReg() {
	    // Incrementar PC para o valor imediato
	    this.incPc();
	    
	    // Ler valor imediato da memória
	    PC.read();
	    memory.read();
	    memory.read();
	    ula.store(0); // Operando 1: valor imediato
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read();
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Operando 2: valor do registrador
	    
	    // Realizar adição na ULA
	    ula.add();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1);
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}




	//SUBS


	public void subRegReg() {
	    // Incrementar PC para o registrador de origem
	    this.incPc();
	    
	    // Ler ID do registrador de origem
	    PC.read();
	    memory.read();
	
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(0); // Operando 1: valor do registrador de origem
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read();
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Operando 2: valor do registrador de destino
	    
	    // Realizar subtração na ULA
	    ula.sub();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1);
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}

	
	public void subMemReg() {	
		// Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler valor da memória
	    PC.read(); 
	    memory.read();
	    memory.read();
	    ula.store(0); // Operando 1: valor da memória
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read(); 
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get()); 
	    registersInternalRead(); 
	    ula.store(1); // Operando 2: valor do registrador
	    
	    // Realizar subtração na ULA
	    ula.sub();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1);
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}
	
	
	public void subRegMem() {
		// Incrementar PC para o registrador de origem
	    this.incPc();
	    
	    // Ler ID do registrador de origem
	    PC.read(); 
	    memory.read();
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Operando 1: valor do registrador
	    
	    // Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler valor da memória
	    PC.read();
	    memory.read(); 
	    memory.read(); 
	    ula.store(1); // Operando 2: valor da memória
	    
	    // Realizar subtração na ULA
	    ula.add();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado na memória
	    ula.read(1);
	    memory.store(); // Obs.:  como a memoria sabe o endereco para armazenar ?
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}
	
	public void subImmReg() {
		// Incrementar PC para o valor imediato
	    this.incPc();
	    
	    // Ler valor imediato da memória
	    PC.read();
	    memory.read();
	    ula.store(0); // Operando 1: valor imediato
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read();
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Operando 2: valor do registrador
	    
	    // Realizar subtração na ULA
	    ula.sub();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1);
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}


	
	//IMULS
	
	public void imulRegMem() {}
	
	public void imulMemReg() {}
	
	public void imulRegReg() {}
	
	
	
	//MOVES	

	public void moveRegReg() {
	    // Incrementar PC para o registrador de origem
	    this.incPc();
	    
	    // Ler ID do registrador de origem
	    PC.read(); 
	    memory.read(); // ID do registrador de origem está agora no extbus1
	    
	    // Selecionar registrador de origem e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador de origem armazenado na ULA
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read(); // ID do registrador de destino está agora no extbus1
	    
	    // Selecionar registrador de destino
	    demux.put(extbus1.get());
	    ula.internalRead(0); // Valor da ULA armazenado no barramento interno 1
	    
	    // Armazenar valor no registrador de destino
	    registersInternalStore(); 
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}

	public void moveMemReg() {
	    // Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler endereço da memória e armazená-lo
	    PC.read(); 
	    memory.read(); // O endereço de memória está agora no extbus1
	    
	    // Ler valor da memória
	    memory.read();
	    ula.store(0); // Valor da memória armazenado na ULA
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read(); // ID do registrador de destino está agora no extbus1
	    
	    // Selecionar registrador de destino
	    demux.put(extbus1.get());
	    ula.internalRead(0); // Valor da ULA armazenado no barramento interno 1
	    
	    // Armazenar valor no registrador de destino
	    registersInternalStore(); 
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}


	public void moveRegMem() {
	    // Incrementar PC para o registrador de origem
	    this.incPc();
	    
	    // Ler ID do registrador de origem
	    PC.read(); 
	    memory.read(); // ID do registrador de origem está agora no extbus1
	    
	    // Selecionar registrador de origem e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador armazenado na ULA
	    
	    // Incrementar PC para o endereço na memória
	    this.incPc();
	    
	    // Ler endereço da memória
	    PC.read();
	    memory.read(); // Endereço de memória está agora no extbus1
	    
	    // Ler o valor armazenado na ULA
	    ula.internalRead(0); // Lê o valor da ULA e coloca no barramento interno 1
	    
	    // Armazenar o valor no endereço de memória
	    memory.store(); // Armazenar o valor no endereço de memória especificado pelo extbus1
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}

	

	public void moveImmReg() {
	    // Incrementar PC para o valor imediato
	    this.incPc();
	    
	    // Ler valor imediato da memória
	    PC.read(); 
	    memory.read(); // Valor imediato está agora no extbus1
	    memory.read(); 
	    
	    // Armazenar valor imediato na ULA
	    ula.store(0); // Valor imediato armazenado na ULA
	    
	    // Incrementar PC para o registrador de destino
	    this.incPc();
	    
	    // Ler ID do registrador de destino
	    PC.read();
	    memory.read(); // ID do registrador de destino está agora no extbus1
	    
	    // Selecionar registrador de destino
	    demux.put(extbus1.get());
	    
	    // Ler valor imediato armazenado na ULA e colocar no barramento interno 1
	    ula.internalRead(0);
	    
	    // Armazenar valor no registrador de destino
	    registersInternalStore();
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}


	public void incReg() {
	    // Incrementar PC para apontar para o registrador
	    this.incPc();
	    
	    // Ler ID do registrador
	    PC.read(); 
	    memory.read(); // ID do registrador está agora no extbus1
	    
	    // Selecionar registrador e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador armazenado na ULA
	    
	    // Incrementar valor na ULA
	    ula.inc();
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Armazenar resultado no registrador de destino
	    ula.internalRead(1); // Lê o valor incrementado da ULA
	    registersInternalStore(); // Armazena o resultado no registrador
	    
	    // Incrementar PC para a próxima instrução
	    this.incPc();
	}

	
	public void jmp() {
	    // Incrementar PC para apontar para o endereço de destino
	    this.incPc();
	    
	    // Ler endereço de destino da memória
	    PC.read(); 
	    memory.read(); // Endereço de destino está agora no extbus1
	    
	    // Armazenar o endereço de destino no PC
	    PC.store(); // Atualizar o PC com o novo endereço
	}
	
	public void jn() {
		 // Incrementar PC para apontar para o endereço de destino
		 this.incPc();
	    // Verificar se o bit de sinal (negativo) está setado
	    if (Flags.getBit(1) == 1) { // Assumindo que o bit de sinal negativo é o bit 1	       
	        
	        // Ler endereço de destino da memória
	        PC.read(); 
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se o bit de sinal não estiver setado, apenas incrementar PC para a próxima instrução
	        this.incPc();
	    }
	}
	
	public void jz() {
		// Incrementar PC para apontar para o endereço de destino
        this.incPc();
	    // Verificar se o bit zero está setado
	    if (Flags.getBit(0) == 1) { // Assumindo que o bit zero é o bit 0        
	        // Ler endereço de destino da memória
	        PC.read(); 
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se o bit zero não estiver setado, apenas incrementar PC para a próxima instrução
	        this.incPc();
	    }
	}
	
	
	public void jeq() {
	    // Incrementar PC para apontar para o primeiro registrador (RegA)
	    this.incPc();
	    
	    // Ler ID do primeiro registrador (RegA)
	    PC.read(); 
	    memory.read(); // ID do registrador RegA está agora no extbus1
	    
	    // Selecionar registrador RegA e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador RegA armazenado na ULA
	    
	    // Incrementar PC para apontar para o segundo registrador (RegB)
	    this.incPc();
	    
	    // Ler ID do segundo registrador (RegB)
	    PC.read();
	    memory.read(); // ID do registrador RegB está agora no extbus1 
	    
	    // Selecionar registrador RegB e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Valor do registrador RegB armazenado na ULA
	    
	    // Realizar subtração na ULA
	    ula.sub(); // Subtrair os valores armazenados na ULA
	    
	    // Verificar se o resultado da subtração é zero
	    setStatusFlags(intbus1.get()); // Atualiza os flags de status
	    if (Flags.getBit(0) == 1) { // Assumindo que o bit zero (Z) indica igualdade
	        // Incrementar PC para apontar para o endereço de destino
	        this.incPc();
	        
	        // Ler endereço de destino da memória
	        PC.read();
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se os valores não forem iguais, apenas incrementar PC para a próxima instrução
	        this.incPc();
	    }
	}


	
	public void jneq() {
	    // Incrementar PC para apontar para o primeiro registrador (RegA)
	    this.incPc();
	    
	    // Ler ID do primeiro registrador (RegA)
	    PC.read(); 
	    memory.read(); // ID do registrador RegA está agora no extbus1
	    
	    // Selecionar registrador RegA e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador RegA armazenado na ULA
	    
	    // Incrementar PC para apontar para o segundo registrador (RegB)
	    this.incPc();
	    
	    // Ler ID do segundo registrador (RegB)
	    PC.read();
	    memory.read(); // ID do registrador RegB está agora no extbus1
	    
	    // Selecionar registrador RegB e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Valor do registrador RegB armazenado na ULA
	    
	    // Realizar subtração na ULA
	    ula.sub(); // Subtrair os valores armazenados na ULA
	    
	    // Verificar se o resultado da subtração não é zero
	    setStatusFlags(intbus1.get()); // Atualiza os flags de status
	    if (Flags.getBit(0) == 0) { // Assumindo que o bit zero (Z) indica igualdade, então verificamos se não é zero
	        // Incrementar PC para apontar para o endereço de destino
	        this.incPc();
	        
	        // Ler endereço de destino da memória
	        PC.read();
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se os valores forem iguais, apenas incrementar PC para a próxima instrução
	        this.incPc();
	    }
	}
	
	public void jgt() {
	    // Incrementar PC para apontar para o primeiro registrador (RegA)
	    this.incPc();
	    
	    // Ler ID do primeiro registrador (RegA)
	    PC.read(); 
	    memory.read(); // ID do registrador RegA está agora no extbus1
	    
	    // Selecionar registrador RegA e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador RegA armazenado na ULA
	    
	    // Incrementar PC para apontar para o segundo registrador (RegB)
	    this.incPc();
	    
	    // Ler ID do segundo registrador (RegB)
	    PC.read();
	    memory.read(); // ID do registrador RegB está agora no extbus1
	    
	    // Selecionar registrador RegB e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Valor do registrador RegB armazenado na ULA
	    
	    // Realizar subtração na ULA
	    ula.sub(); // Subtrair RegA - RegB
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Verificar se o resultado da subtração não é negativo
	    if (Flags.getBit(1) == 0) { // Assumindo que o bit negativo é o bit 1
	        // Incrementar PC para apontar para o endereço de destino
	        this.incPc();
	        
	        // Ler endereço de destino da memória
	        PC.read();
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.put(extbus1.get()); // Colocar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se o resultado for negativo, apenas incrementar PC para a próxima instrução
	        this.incPc();
	        this.incPc();
	    }
	}

	public void jlw() {
	    // Incrementar PC para apontar para o primeiro registrador (RegA)
	    this.incPc();
	    
	    // Ler ID do primeiro registrador (RegA)
	    PC.read(); 
	    memory.read(); // ID do registrador RegA está agora no extbus1
	    
	    // Selecionar registrador RegA e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead(); 
	    ula.store(0); // Valor do registrador RegA armazenado na ULA
	    
	    // Incrementar PC para apontar para o segundo registrador (RegB)
	    this.incPc();
	    
	    // Ler ID do segundo registrador (RegB)
	    PC.read();
	    memory.read(); // ID do registrador RegB está agora no extbus1
	    
	    // Selecionar registrador RegB e ler seu valor
	    demux.put(extbus1.get());
	    registersInternalRead();
	    ula.store(1); // Valor do registrador RegB armazenado na ULA
	    
	    // Realizar subtração na ULA
	    ula.sub(); // Subtrair RegA - RegB
	    
	    // Atualizar flags de status
	    setStatusFlags(intbus1.get());
	    
	    // Verificar se o resultado da subtração é negativo
	    if (Flags.getBit(1) == 1) { // Assumindo que o bit negativo é o bit 1
	        // Incrementar PC para apontar para o endereço de destino
	        this.incPc();
	        
	        // Ler endereço de destino da memória
	        PC.read();
	        memory.read(); // Endereço de destino está agora no extbus1
	        
	        // Armazenar o endereço de destino no PC
	        PC.store(); // Atualizar o PC com o novo endereço
	    } else {
	        // Se o resultado não for negativo, apenas incrementar PC para a próxima instrução
	        this.incPc();
	        this.incPc();
	    }
	}
	

	public void read() {
		
	}
	public void store() {
		
	}
	
	public void ldi() {
		
	}


	
	




	
	

	

	/**
	 * This method implements the microprogram for
	 * 					SUB address
	 * In the machine language this command number is 1, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture
	 * The method reads the value from memory (position address) and 
	 * performs an SUB with this value and that one stored in the rpg (the first register in the register list).
	 * The final result must be in RPG (the first register in the register list).
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. rpg -> intbus1 //rpg.read() the current rpg value must go to the ula 
	 * 7. ula <- intbus1 //ula.store()
	 * 8. pc -> extbus (pc.read())
	 * 9. memory reads from extbus //this forces memory to write the data position in the extbus
	 * 10. memory reads from extbus //this forces memory to write the data value in the extbus
	 * 11. rpg <- extbus (rpg.store())
	 * 12. rpg -> intbus1 (rpg.read())
	 * 13. ula  <- intbus1 //ula.store()
	 * 14. Flags <- zero //the status flags are reset
	 * 15. ula subs
	 * 16. ula -> intbus1 //ula.read()
	 * 17. ChangeFlags //informations about flags are set according the result 
	 * 18. rpg <- intbus1 //rpg.store() - the add is complete.
	 * 19. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 20. ula <- intbus2 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus2 //ula.read()
	 * 23. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */
/*
	public void sub() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		RPG.internalRead();
		ula.store(0); //the rpg value is in ULA (0). This is the first parameter
		PC.read(); 
		memory.read(); // the parameter is now in the external bus. 
						//but the parameter is an address and we need the value
		memory.read(); //now the value is in the external bus
		RPG.store();
		RPG.internalRead();
		ula.store(1); //the rpg value is in ULA (0). This is the second parameter
		ula.sub(); //the result is in the second ula's internal register
		ula.internalRead(1);; //the operation result is in the internalbus 2
		setStatusFlags(intbus2.get()); //changing flags due the end of the operation
		RPG.internalStore(); //now the sub is complete
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	*/
	
	/**
	 * This method implements the microprogram for
	 * 					JMP address
	 * In the machine language this command number is 2, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where the PC is redirecto to)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register.
	 * So, the program is deviated
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //pc.read()
	 * 7. memory reads from extbus //this forces memory to write the data position in the extbus
	 * 8. pc <- extbus //pc.store() //pc was pointing to another part of the memory
	 * end
	 * @param address
	 */
/*
	public void jmp() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read();
		memory.read();
		PC.store();
	}
	*/
	
	/**
	 * This method implements the microprogram for
	 * 					JZ address
	 * In the machine language this command number is 3, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where 
	 * the PC is redirected to, but only in the case the ZERO bit in Flags is 1)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register if the ZERO bit in Flags register is setted.
	 * So, the program is deviated conditionally
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. If FLAGS.bit0 is 1 (bit 0 is ZERO flag) then PC must be updated according the parameter
	 * 7. 	pc -> extbus //pc.read()
	 * 8. 	memory reads from extbus //this forces memory to write the data position in the extbus
	 * 9. 	pc <- extbus //pc.store() //pc was pointing to another part of the memory
	 * 10.ELSE //Flags.Bit0 is not 0. So, PC must be incremented to the next position
	 * 11.  ula incs //the position just after PC
	 * 12.  ula -> intbus2 //uma.read()
	 * 13.  pc <- intbus2 
	 * end
	 * @param address
	 */
/*
	public void jz() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		if (Flags.getBit(0)==1) { 
			PC.read();
			memory.read();
			PC.store();
		}
		else {
			ula.inc();
			ula.internalRead(1);
			PC.internalStore();
		}
	}
	*/
	
	/**
	 * This method implements the microprogram for
	 * 					jn address
	 * In the machine language this command number is 4, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where 
	 * the PC is redirected to, but only in the case the NEGATIVE bit in Flags is 1)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register if the NEG bit in Flags register is setted.
	 * So, the program is deviated conditionally
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. If FLAGS.bit1 is 1 (bit 1 is NEGATIVE flag) then PC must be updated according the parameter
	 * 7. 	pc -> extbus //pc.read()
	 * 8. 	memory reads from extbus //this forces memory to write the data position in the extbus
	 * 9. 	pc <- extbus //pc.store() //pc was pointing to another part of the memory
	 * 10.ELSE //Flags.Bit1 is not 0. So, PC must be incremented to the next position
	 * 11.  ula incs //the position just after PC
	 * 12.  ula -> intbus2 //ula.read()
	 * 13.  pc <- intbus2 
	 * end
	 * @param address
	 */

/*
	public void jn() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		if (Flags.getBit(1)==1) { 
			PC.read();
			memory.read();
			PC.store();
		}
		else {
			ula.inc();
			ula.internalRead(1);
			PC.internalStore();
		}
	}
	*/
	
	/**
	 * This method implements the microprogram for
	 * 					read address
	 * In the machine language this command number is 5, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture 
	 * The method reads the value from memory (position address) and 
	 * inserts it into the RPG register (the first register in the register list)
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the address in the extbus
	 * 8. memory reads from extbus //this forces memory to write the stored data in the extbus
	 * 9. RPG <- extbus //the data is read
	 * 10. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 11. ula <- intbus2 //ula.store()
	 * 12. ula incs
	 * 13. ula -> intbus2 //ula.read()
	 * 14. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */

/*
	public void read() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the address is now in the external bus.
		memory.read(); // the data is now in the external bus.
		RPG.store();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	*/

	
	/**
	 * This method implements the microprogram for
	 * 					store address
	 * In the machine language this command number is 6, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture 
	 * The method reads the value from RPG (the first register in the register list) and 
	 * inserts it into the memory (position address) 
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the parameter address is the external bus
	 * 7. memory reads // memory reads the data in the parameter address. 
	 * 					// this data is the address where the RPG value must be stores 
	 * 8. memory stores //memory reads the address and wait for the value
	 * 9. RPG -> Externalbus //RPG.read()
	 * 10. memory stores //memory receives the value and stores it
	 * 11. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 12. ula <- intbus2 //ula.store()
	 * 13. ula incs
	 * 14. ula -> intbus2 //ula.read()
	 * 15. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */

/*
	public void store() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read();   //the parameter address (pointing to the addres where data must be stored
		                 //is now in externalbus1
		memory.store(); //the address is in the memory. Now we must to send the data
		RPG0.read();
		memory.store(); //the data is now stored
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	*/

	
	/**
	 * This method implements the microprogram for
	 * 					ldi immediate
	 * In the machine language this command number is 7, and the immediate value
	 *        is in the position next to him
	 *    
	 * The method moves the value (parameter) into the internalbus1 and the RPG 
	 * (the first register in the register list) consumes it 
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the stored data in the extbus
	 * 8. RPG <- extbus //rpg.store()
	 * 9. 10. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 10. ula <- intbus2 //ula.store()
	 * 11. ula incs
	 * 12. ula -> intbus2 //ula.read()
	 * 13. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */

/*
	public void ldi() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the immediate is now in the external bus.
		RPG.store();   //RPG receives the immediate
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	*/


	
	/**
	 * This method implements the microprogram for
	 * 					inc 
	 * In the machine language this command number is 8
	 *    
	 * The method moves the value in rpg (the first register in the register list)
	 *  into the ula and performs an inc method
	 * 		-> inc works just like add rpg (the first register in the register list)
	 *         with the mumber 1 stored into the memory
	 * 		-> however, inc consumes lower amount of cycles  
	 * 
	 * The logic is
	 * 
	 * 1. rpg -> intbus1 //rpg.read()
	 * 2. ula  <- intbus1 //ula.store()
	 * 3. Flags <- zero //the status flags are reset
	 * 4. ula incs
	 * 5. ula -> intbus1 //ula.read()
	 * 6. ChangeFlags //informations about flags are set according the result
	 * 7. rpg <- intbus1 //rpg.store()
	 * 8. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 9. ula <- intbus2 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus2 //ula.read()
	 * 12. pc <- intbus2 //pc.store()
	 * end
	 * @param address
	 */

/*
	public void inc() {
		RPG.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		setStatusFlags(intbus1.get());
		RPG.internalStore();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	*/


	
	/**
	 * This method implements the microprogram for
	 * 					move <reg1> <reg2> 
	 * In the machine language this command number is 9
	 *    
	 * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
	 * copies the value from the <reg1> register to the <reg2> register
	 * 
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the first parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the parameter (first regID) in the extbus
	 * 8. pc -> intbus2 //pc.read() //getting the second parameter
	 * 9. ula <-  intbus2 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus2 //ula.read()
	 * 12. pc <- intbus2 //pc.store() now pc points to the second parameter
	 * 13. demux <- extbus //now the register to be operated is selected
	 * 14. registers -> intbus1 //this performs the internal reading of the selected register 
	 * 15. PC -> extbus (pc.read())the address where is the position to be read is now in the external bus 
	 * 16. memory reads from extbus //this forces memory to write the parameter (second regID) in the extbus
	 * 17. demux <- extbus //now the register to be operated is selected
	 * 18. registers <- intbus1 //thid rerforms the external reading of the register identified in the extbus
	 * 19. 10. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 20. ula <- intbus2 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus2 //ula.read()
	 * 23. pc <- intbus2 //pc.store()  
	 * 		  
	 */

/*
	public void moveRegReg() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the first parameter (the first reg id)
		
		
		PC.read(); 
		memory.read(); // the first register id is now in the external bus.
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the second parameter (the second reg id)
		demux.put(extbus1.get()); //points to the correct register
		registersInternalRead(); //starts the read from the register identified into demux bus
		PC.read();
		memory.read(); // the second register id is now in the external bus.
		demux.put(extbus1.get());//points to the correct register
		registersInternalStore(); //performs an internal store for the register identified into demux bus
		
		
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	*/
	
	
	public ArrayList<Register> getRegistersList() {
		return registersList;
	}

	/**
	 * This method performs an (external) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersRead() {
		registersList.get(demux.get()).read();
	}
	
	/**
	 * This method performs an (internal) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalRead() {
		registersList.get(demux.get()).internalRead();;
	}
	
	/**
	 * This method performs an (external) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersStore() {
		registersList.get(demux.get()).store();
	}
	
	/**
	 * This method performs an (internal) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalStore() {
		registersList.get(demux.get()).internalStore();;
	}



	/**
	 * This method reads an entire file in machine code and
	 * stores it into the memory
	 * NOT TESTED
	 * @param filename
	 * @throws IOException 
	 */
	public void readExec(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new		 
		   FileReader(filename+".dxf"));
		   String linha;
		   int i=0;
		   while ((linha = br.readLine()) != null) {
			     extbus1.put(i);
			     memory.store();
			   	 extbus1.put(Integer.parseInt(linha));
			     memory.store();
			     i++;
			}
			br.close();
	}
	
	/**
	 * This method executes a program that is stored in the memory
	 */
	public void controlUnitEexec() {
		halt = false;
		while (!halt) {
			fetch();
			decodeExecute();
		}

	}
	

	/**
	 * This method implements The decode proccess,
	 * that is to find the correct operation do be executed
	 * according the command.
	 * And the execute proccess, that is the execution itself of the command
	 */
	private void decodeExecute() {
		IR.internalRead(); // the instruction is in the internalbus1
		int command = intbus1.get();
		simulationDecodeExecuteBefore(command);
		switch (command) {
			case 0:
				addRegReg();
				break;
			case 1:
				addMemReg();
				break;
			case 2:
				addRegMem();
				break;
			case 3:
				addImmReg();
				break;
				
			case 4:
				subRegReg();
				break;
			case 5:
				subMemReg();
				break;
			case 6:
				subRegMem();
				break;
			case 7:
				subImmReg();
				break;
				
			case 8:
				imulMemReg();
				break;
			case 9:
				imulRegMem();
				break;
			case 10:
				imulRegReg();
				break;
				
			case 11:
				moveRegReg();				
				break;
			case 12:
				moveMemReg();				
				break;
			case 13:
				moveRegMem();
				break;
			case 14:
				moveImmReg();
				break;
				
			case 15:
				incReg();
				break;
				
			case 16:
				jmp();
				break;
			case 17:
				jn();
				break;
			case 18:
				jz();
				break;
			case 19:
				jeq();
				break;
			case 20:
				jneq();
				break;
			case 21:
				jgt();
				break;
			case 22:
				jlw();
				break;			
			case 23:
				read();
				break;
			case 24:
				store();
				break;
			case 25:
				ldi();
				break;
			default:
				halt = true;
				break;
		}
		if (simulation)
			simulationDecodeExecuteAfter();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 * @param command 
	 */
	private void simulationDecodeExecuteBefore(int command) {
		System.out.println("----------BEFORE Decode and Execute phases--------------");
		String instruction;
		int parameter = 0;
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		if (command !=-1)
			instruction = commandsList.get(command);
		else
			instruction = "END";
		if (hasOperands(instruction)) {
			parameter = memory.getDataList()[PC.getData()+1];
			System.out.println("Instruction: "+instruction+" "+parameter);
		}
		else
			System.out.println("Instruction: "+instruction);
		if ("read".equals(instruction))
			System.out.println("memory["+parameter+"]="+memory.getDataList()[parameter]);
		
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED 
	 */
	private void simulationDecodeExecuteAfter() {
		String instruction;
		System.out.println("-----------AFTER Decode and Execute phases--------------");
		System.out.println("Internal Bus 1: "+intbus1.get());;
		System.out.println("External Bus 1: "+extbus1.get());
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		Scanner entrada = new Scanner(System.in);
		System.out.println("Press <Enter>");
		String mensagem = entrada.nextLine();
	}

	/**
	 * This method uses PC to find, in the memory,
	 * the command code that must be executed.
	 * This command must be stored in IR
	 * NOT TESTED!
	 */
	private void fetch() {
		PC.read();
		memory.read();
		IR.store();
		simulationFetch();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED!!!!!!!!!
	 */
	private void simulationFetch() {
		if (simulation) {
			System.out.println("-------Fetch Phase------");
			System.out.println("PC: "+PC.getData());
			System.out.println("IR: "+IR.getData());
		}
	}

	/**
	 * This method is used to show in a correct way the operands (if there is any) of instruction,
	 * when in simulation mode
	 * NOT TESTED!!!!!
	 * @param instruction 
	 * @return
	 */
	private boolean hasOperands(String instruction) {
		if ("inc".equals(instruction)) //inc is the only one instruction having no operands
			return false;
		else
			return true;
	}

	/**
	 * This method returns the amount of positions allowed in the memory
	 * of this architecture
	 * NOT TESTED!!!!!!!
	 * @return
	 */
	public int getMemorySize() {
		return memorySize;
	}
	
	public static void main(String[] args) throws IOException {
		Architecture2 arch = new Architecture2(true);
		arch.readExec("program");
		arch.controlUnitEexec();
	}
	

}
