package edu.utexas.cs.tamerProject.featGen.moral;

import java.util.ArrayList;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.featGen.tetris.*;


//import edu.utexas.cs.tamerProject.modeling.RegressionModel;

public class MoralFeatGen_Tetris extends FeatGenerator{

	TetrisState gameState;
	
	int NUM_COLORS = 3;
//	int NUM_FEATS = ((NUM_COLORS)*(NUM_COLORS+1)) + 2;
	int NUM_FEATS = ((NUM_COLORS)*(NUM_COLORS+1))/2;//+ 1;
//	final int NUM_BLOCKS_I = 0;
	final int NUM_ADJ_COL_START_I = 0;
//	final int SQUARED_FEATS_START_I = ((NUM_COLORS)*(NUM_COLORS+1))/2 + 1;

	public static double HT_SQ_SCALE = 100.0; // 40 in python-based code and experiments before 2013-02-21

	/**
	 *  python-based code used integer division and chopped off the decimal.
	 *  Set to true to get identical behavior to the python agent when learning
	 *  from python-trained logs. 
	 */
	public static boolean REMOVE_DECIMAL = false;
	/*
	 * python-based code has a bug where the last column's well (if there is one)
	 * isn't counted towards the sum of wells feature. Since the data was collected
	 * with this bug, this should be set to false when learning from python logs.
	 * (However, the python logs I've tested actually do much better on average when
	 * the last column IS counted.)
	 */
	public static boolean COUNT_LAST_COL_FOR_WELL_SUM = true;
	
	/*
	 * python-based code and Tetris results before 2013-02-21 only scale the squared 
	 * height features. However, the huge size of some other squared features are 
	 * causing the model update to overshoot at higher step sizes (which I'd like to
	 * use to be more accurate in saying we're creating a "predictive model" of the 
	 * human), causing values to self-increase to infinity. 
	 */
	public static boolean SCALE_ALL_SQUARED_FEATS = false;
	
	double[] currSFeats;
	double[] nextSFeats;
	
	public MoralFeatGen_Tetris(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges){
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		
		gameState = new TetrisState();		// initiate Tetris game
		
		this.numFeatures = NUM_FEATS;
		this.currSFeats = new double[NUM_FEATS];
		this.nextSFeats = new double[NUM_FEATS];
	}
	
	public ArrayList<Action> getPossActions(Observation obs){
		//// search for possible actions
		this.setStateFromObs(obs);
		this.getSFeats(obs);
		//gameState.printStateWCurrentPiece();
		ArrayList<ExtendedTetrisAction> extendedActions = this.getExtendedActList(obs.intArray);
		this.actList.clearActionList();
		for (ExtendedTetrisAction extendedAction: extendedActions) {
			Action thisExtAction = new Action();
			thisExtAction.intArray = extendedAction.actList;
			this.actList.addActionToList(thisExtAction);
		}
		if (this.actList.getActionList().size() == 0) {
			System.err.println("Zero actions returned by Tetris state. Check that the modified Tetris, " +
					"which communicates the full state, is being used (not the RL-Library version).");
			System.exit(1);
		}
		return this.actList.getActionList();
	}
	
	
	private void setStateFromObs(Observation obs) {
		for (int i = 0; i < gameState.worldState.length; i++)
			gameState.worldState[i] = obs.intArray[i];
        gameState.blockMobile = obs.intArray[gameState.worldState.length] == 1;
        gameState.currentBlockId = obs.intArray[gameState.worldState.length + 1];
	    gameState.currentRotation = obs.intArray[gameState.worldState.length + 2];
    	gameState.currentX = obs.intArray[gameState.worldState.length + 3];
    	gameState.currentY = obs.intArray[gameState.worldState.length + 4];
	    gameState.worldWidth = obs.intArray[gameState.worldState.length + 5];
        gameState.worldHeight = obs.intArray[gameState.worldState.length + 6];
        gameState.currentBlockColorId = obs.intArray[gameState.worldState.length + 7];
	}
	
	public double[] getSAFeats(Observation obs, Action act){
		this.setStateFromObs(obs);
//		gameState.printState(); //// print state for debugging
		//// use state vars and action to determine next state (after line clear)
		for (int actI = 0; actI < act.intArray.length; actI++){ //// take actions
			this.gameState.take_action(act.intArray[actI]); 
			this.gameState.update();
		}
		this.gameState.writeCurrentBlock(this.gameState.worldState, true);
        this.gameState.checkIfRowAndScore();
//		gameState.printState(); //// print state for debugging
		
		//// find features from difference in states; could call getSSFeats for this
		return this.getSSFeats(obs.intArray, obs.doubleArray, this.gameState.worldState, new double[0]);
	}
	
	public double[] getSSFeats(int[] intStateVars, double[] doubleStateVars, int[] intNextStateVars, double[] doubleNextStateVars){
		this.putSFeatsInArray(intStateVars, this.currSFeats); //// how could i cleanly reuse this for each potential action?
//		System.out.println("currSFeats: " + Arrays.toString(this.currSFeats));
		this.putSFeatsInArray(intNextStateVars, this.nextSFeats);
//		System.out.println("nextSFeats: " + Arrays.toString(this.nextSFeats));
		double[] ssFeats = new double[NUM_FEATS];
		for (int i = 0; i < NUM_FEATS; i++) {
			ssFeats[i] = this.nextSFeats[i] - this.currSFeats[i];
		}
		//System.out.println("ssFeats: " + Arrays.toString(ssFeats));
		return ssFeats;
	}
	
	public double[] getSFeats(Observation obs){
		double[] featsArray = new double[NUM_FEATS];
		this.putSFeatsInArray(obs.intArray, featsArray);
		return featsArray;
	}
	private void putSFeatsInArray(int[] intStateVars, double[] featsArray){
		
		for (int i = 0; i < featsArray.length; i++) {
			featsArray[i] = 0;
		}
//		int count = 0;
//		for(int i = 0; i < gameState.worldState.length; ++i)
//			if (intStateVars[i] != 0) 
//				++count;
//		featsArray[NUM_BLOCKS_I] = count;
		// iterate through cells
		int[] worldState = intStateVars;
		int featIndex = 0;
		for(int color1 = 1; color1 <= NUM_COLORS; ++color1)
		{
			for(int color2 = color1; color2 <= NUM_COLORS; ++color2)
			{
				for (int row = 0; row < gameState.getHeight(); row++) 
				{
					for (int col = 0; col < gameState.getWidth(); col++) 
					{
						int here = worldState[getIndex(row, col)];
						int bottom = -1;
						int right = -1;
						if(row < gameState.getHeight() - 1)
							bottom = worldState[getIndex(row + 1, col)];
						if(col < gameState.getWidth() - 1)
							right = worldState[getIndex(row, col + 1)];
						if(here == color1 && bottom == color2
							|| here == color2 && bottom == color1)
							featsArray[NUM_ADJ_COL_START_I + featIndex] += 1;
						if(here == color1 && right == color2
							|| here == color2 && right == color1)
							featsArray[NUM_ADJ_COL_START_I + featIndex] += 1;
					}
				}
				++featIndex;
			}
		}
//		
//		for (int i = 0; i < SQUARED_FEATS_START_I; i++) {
//			featsArray[SQUARED_FEATS_START_I + i] = Math.pow(featsArray[i], 2.0);
//			if (SCALE_ALL_SQUARED_FEATS) {
//				featsArray[SQUARED_FEATS_START_I + i] /= HT_SQ_SCALE;
//			}
//			if (REMOVE_DECIMAL)
//				featsArray[SQUARED_FEATS_START_I + i] = (int)featsArray[SQUARED_FEATS_START_I + i];
//			
//		}
		//System.out.println("featsArray: " + Arrays.toString(featsArray));
		//System.out.println("NUM_HOLES:" + featsArray[NUM_HOLES_I]);
		//System.out.println("MAX_WELL: " + featsArray[MAX_WELL_I]);
		//System.out.println("SUM_WELL: " + featsArray[SUM_WELL_I]);
	}

	private int getIndex(int row, int col) {
		return (row * gameState.getWidth()) + col;
	}

	//// this method does a tree search to find all possible piece placements 
	public ArrayList<ExtendedTetrisAction> getExtendedActList(int[] intObsVals) {
		int[] actOrder = {4, 0, 1, 2, 3};
		ArrayList<ExtendedTetrisAction> extendedActList = new ArrayList<ExtendedTetrisAction>();
		TetrisState tempState = new TetrisState(this.gameState);		//// copy world state
		ExtendedTetrisAction startMove = new ExtendedTetrisAction(tempState.currentRotation, tempState.currentX, tempState.currentY, tempState.blockMobile, new int[0]);
		
		ArrayList<ExtendedTetrisAction> liveMoves = new ArrayList<ExtendedTetrisAction>();
		liveMoves.add(startMove);
		
		//// search tree of possible moves, pruning duplicates
		//System.out.println();
		while (liveMoves.size() > 0) {
			//System.out.println("----------------------\n----------------------\n----------------------");
			//System.out.println("liveMoves.size(): " + liveMoves.size());
			ArrayList<ExtendedTetrisAction> nextLevelLiveMoves = new ArrayList<ExtendedTetrisAction>();
			for (int actNum: actOrder) { //// make each possible move
				for (ExtendedTetrisAction liveMove: liveMoves) {
					//System.out.println();
					liveMove.setState(tempState);
					if (!tempState.take_action(actNum)) // take action and skip if action was illegal
						continue;
					//System.out.print("blockMobile before: " + tempState.blockMobile + ", ");
					tempState.update();
					
					//// if move is duplicate, continue
					if (isInExtActList(tempState, extendedActList)) {
						//System.out.print("duplicate of finished extended act, ");
						continue;
					}
					if((isInExtActList(tempState, nextLevelLiveMoves) || 
							isInExtActList(tempState, liveMoves)) && tempState.blockMobile)  {
						//System.out.print("duplicate of live extended act, ");
						continue;
					}
						
					
					//// make new extended action
					int[] thisActList = Arrays.copyOf(liveMove.actList, liveMove.actList.length + 1);
					thisActList[thisActList.length - 1] = actNum;
					ExtendedTetrisAction thisMove = new ExtendedTetrisAction(tempState.currentRotation, tempState.currentX, 
																			 tempState.currentY, tempState.blockMobile, thisActList);
					//System.out.print("blockMobile after: " + tempState.blockMobile + ", ");
					if (tempState.blockMobile) {	//// if block is active, make ExtendedTetrisAction and add to liveMoves
						nextLevelLiveMoves.add(thisMove);
						//tempState.printStateWCurrentPiece();
					}
					else {		//// if move is inactive, add to extendedActList
						extendedActList.add(thisMove);
						//System.out.println("Placement:");
						//tempState.printStateWCurrentPiece();
					}
				}
				//// remove last live branch
				//liveMoves.remove(liveMove);
			}
			liveMoves = nextLevelLiveMoves;
		}
		//// add a no-action
		for (ExtendedTetrisAction extAct: extendedActList){
			extAct.addNoneAction();
		}
		//System.out.println("extendedActList.size():" + extendedActList.size());
		return extendedActList;
	}
	
	public boolean isInExtActList(TetrisState state, ArrayList<ExtendedTetrisAction> moves) {
		boolean inList = false;
		for (ExtendedTetrisAction move: moves) {
			if (state.equivRotation(move.currentRotation) &&
					state.currentX == move.currentX &&
					state.currentY == move.currentY)
				inList = true;
		}
		return inList;
	}
	
	public class ExtendedTetrisAction {
	    int currentRotation;
    	int currentX;
    	int currentY;
    	boolean blockMobile;
    	int[] actList;
    	public ExtendedTetrisAction(int currentRotation, int currentX, int currentY, boolean blockMobile, int[] actList){
    		this.currentRotation = currentRotation;
    		this.currentX = currentX;
    		this.currentY = currentY;
    		this.blockMobile = blockMobile;
    		this.actList = actList;
    	}	
    	public void setState(TetrisState state) {
    		state.currentRotation = currentRotation;
    		state.currentX = currentX;
    		state.currentY = currentY;
    		state.blockMobile = blockMobile;
    	}
    	public void addNoneAction(){
    		this.actList = Arrays.copyOf(this.actList, this.actList.length + 1);
    		this.actList[this.actList.length - 1] = 4;
    	}
	}
	
	
	public int[] getActionFeatIndices(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new int[0];
	}
	
	public int[] getNumFeatValsPerFeatI(){
		// this would be a large number, and it shouldn't be used for any Tetris-specific reasons
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new int[0];
	}
	
	public double[] getSAFeats(int[] intStateVars, double[] doubleStateVars,
			int[] intActVars, double[] doubleActVars) {
		System.err.println("This getSAFeats without o.charArray should not be used for Mario." +
				" Exiting in "+ this.getClass() + ".");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	


	
	
	
	// These currently do not support feature generators with a supplemental model added.
	// Such support is unnecessary as long as this is only used by HInfluence.
	public double[] getMaxPossFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	
	}
	public double[] getMaxPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
}


	




