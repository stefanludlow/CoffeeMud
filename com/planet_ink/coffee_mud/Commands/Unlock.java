package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Unlock extends StdCommand
{
	public Unlock(){}

	private String[] access={"UNLOCK","UNL","UN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String whatTounlock=CMParms.combine(commands,1);
		if(whatTounlock.length()==0)
		{
			mob.tell(getScr("Movement","unlockerr1"));
			return false;
		}
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatTounlock,Item.WORN_REQ_ANY);

		if((unlockThis==null)||(!CMLib.flags().canBeSeenBy(unlockThis,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",whatTounlock));
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_UNLOCK,getScr("Movement","sunlocks")+CMProps.msp("doorunlock.wav",10));
		if(unlockThis instanceof Exit)
		{
			boolean locked=((Exit)unlockThis).isLocked();
			if((mob.location().okMessage(msg.source(),msg))
			&&(locked))
			{
				mob.location().send(msg.source(),msg);
				if(dirCode<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					if(mob.location().getExitInDir(d)==unlockThis)
					{dirCode=d; break;}

				if((dirCode>=0)&&(mob.location().getRoomInDir(dirCode)!=null))
				{
					Room opR=mob.location().getRoomInDir(dirCode);
					Exit opE=mob.location().getPairedExit(dirCode);
					if(opE!=null)
					{
						CMMsg altMsg=CMClass.getMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					int opCode=Directions.getOpDirectionCode(dirCode);
					if((opE!=null)
					&&(!opE.isLocked())
					&&(!((Exit)unlockThis).isLocked()))
					   opR.showHappens(CMMsg.MSG_OK_ACTION,getScr("Movement","afterunlocks",opE.name(),Directions.getInDirectionName(opCode)));
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int actionsCost(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
