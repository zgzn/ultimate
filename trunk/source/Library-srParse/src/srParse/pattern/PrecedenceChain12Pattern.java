package srParse.pattern;

import pea.CDD;

public class PrecedenceChain12Pattern extends PatternType
	{
		public void transform()
		{
			CDD p_cdd = cdds.get(2); 
			CDD q_cdd = scope.getCdd1(); 
			CDD r_cdd = scope.getCdd2();
			CDD s_cdd = cdds.get(1);
			CDD t_cdd = cdds.get(0);
			
			pea = peaTransformator.precedenceChainPattern12(p_cdd, q_cdd, r_cdd, s_cdd, t_cdd, scope.toString());
		}
		
		public String toString()
		{
			String res=new String();
			
			res="it is always the case that if \""+cdds.get(2)+"\" holds and is succeeded by \""+cdds.get(1)+"\", then \""+cdds.get(0)+"\" previously held";
			
			return res;
		}
	}