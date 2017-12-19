import java.util.ArrayList;

public class TxHandler {
	private UTXOPool upool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	upool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	boolean ret_val = true;
    	double input_sum = 0;
    	double output_sum = 0;
    	ArrayList<UTXO> curr_UTXOs = upool.getAllUTXO();
    	 
    	for (Transaction.Output op : tx.getOutputs()) {
    		ret_val = false;
    		output_sum += op.value;
    		if (op.value < 0) { // 4) 
    			break; //return false;
    		}
    		// 1) and 3) 
    		for (int i = 0; i < curr_UTXOs.size(); i++) {
    			Transaction.Output upool_op = upool.getTxOutput(curr_UTXOs.get(i));
    			if (upool_op.value == op.value && upool_op.address == op.address) {
    				curr_UTXOs.remove(i);
    				ret_val = true;
    				break;
    			}
    		}
    	}
    	// 2) 
    	for (Transaction.Input in : tx.getInputs()) {
    		if (in != null) {
	    		Transaction.Output op = tx.getOutput(in.outputIndex);
	    		if (op!=null) {
	        		if (!Crypto.verifySignature(
	        				op.address, 
	        				tx.getRawDataToSign(in.outputIndex), 
	        				in.signature
	        			)) {
	        			ret_val = false;
	        			break;
	        		}
	        		input_sum += op.value;	
	    		}
    		}
    	}
    	
    	if (output_sum > input_sum) { //5)
    		ret_val = false;
    	}
    	
    	return ret_val;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
    	for (Transaction tx : possibleTxs) {
    		if(isValidTx(tx)) {
    			validTxs.add(tx);
    			for(Transaction.Input in : tx.getInputs()) {
    				UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
    				upool.removeUTXO(u);
    			}
    		}
    	}
    	return validTxs.toArray(new Transaction[validTxs.size()]);
    }

}
