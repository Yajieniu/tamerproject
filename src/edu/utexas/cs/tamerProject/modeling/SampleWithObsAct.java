package edu.utexas.cs.tamerProject.modeling;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class SampleWithObsAct extends Sample{

	public Observation obs = null;
	public Action act = null;

	public SampleWithObsAct(double[] feats, double weight) {super(feats, weight);}

	public SampleWithObsAct(double[] feats, double weight, int unique){super(feats, weight, unique);}

	public SampleWithObsAct(double[] feats, double label, double weight){super(feats, label, weight);}

	public SampleWithObsAct(double[] feats, double label, double weight, int unique){super(feats, label, weight, unique);}

	public SampleWithObsAct(double[] feats, double weight, Observation obs, Action act) {
		super(feats, weight);
		this.obs = obs;
		this.act = act;
	}

	public SampleWithObsAct(double[] feats, double weight, int unique, Observation obs, Action act){
		super(feats, weight, unique);
		this.obs = obs;
		this.act = act;
	}

	public SampleWithObsAct(double[] feats, double label, double weight, Observation obs, Action act){
		super(feats, label, weight);
		this.obs = obs;
		this.act = act;
	}

	public SampleWithObsAct(double[] feats, double label, double weight, int unique, Observation obs, Action act){
		super(feats, label, weight, unique);
		this.obs = obs;
		this.act = act;
	}
	
	public SampleWithObsAct(Sample sample, Observation obs, Action act) {
		super(sample.feats, sample.label, sample.weight, sample.unique);
//		this.REGRESSION_SAMPLE = sample.REGRESSION_SAMPLE;
		this.unweightedRew = sample.unweightedRew;
		this.creditUsedLastStep = sample.creditUsedLastStep;
		this.usedCredit = sample.usedCredit;
//		this.feats = sample.feats; TODO remove these
//		this.label = sample.label;
		//		this.weight = sample.weight;
//		 this.unique = sample.unique;
		
		this.obs = obs;
		this.act = act;
	}

	public SampleWithObsAct clone() {
		Sample cloneSampleNoOA = super.clone();
		SampleWithObsAct cloneSample = new SampleWithObsAct(cloneSampleNoOA, this.obs.duplicate(), this.act.duplicate());
		return cloneSample;
	}
	
	public String toString(){
		String s = "\n";
		s += "obs: " + this.obs + "\n";
		s += "act: " + this.act + "\n";
		s += "feats: " + Arrays.toString(feats) + "\n";
		s += "label: " + label + "\n";
		s += "unweighted reward: " + unweightedRew + "\n";
		s += "weight: " + weight + "\n";
		s += "creditUsedLastStep: " + creditUsedLastStep + "\n";
		s += "usedCredit: " + usedCredit + "\n";
		s += "unique: " + unique + "\n";
		return s;
	}
	
	public String debugString(){
		String s = "\n";
		s += "obs: " + Arrays.toString(this.obs.intArray) + "\n";
		s += "act: " + Arrays.toString(this.act.intArray) + "\n";
		//s += "feats: " + Arrays.toString(feats) + "\n";
		s += "label: " + label + "\n";
		s += "unweighted reward: " + unweightedRew + "\n";
		s += "weight: " + weight + "\n";
		s += "creditUsedLastStep: " + creditUsedLastStep + "\n";
		s += "usedCredit: " + usedCredit + "\n";
		s += "unique: " + unique + "\n";
		return s;
	}
	
}
