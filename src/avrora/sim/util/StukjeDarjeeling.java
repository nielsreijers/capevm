package avrora.sim.util;

import avrora.sim.*;
import avrora.arch.legacy.*;
import cck.text.StringUtil;
import cck.text.Terminal;
import avrora.core.SourceMapping;

import java.io.*;

public class StukjeDarjeeling {
	// Rebuilding some small DJ functions here so we can get the name of Java classes.

    static private int getProgramInt8(AtmelInterpreter a, int offset) {
        return a.getProgramByte(offset);
    }
    static private int getDataInt16(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }

	public static class DjGlobalId
	{
		public String infusionName;
		public int infusionPointer;
		public int entity_id;

		public DjGlobalId(AtmelInterpreter a, int infusionPointer, int entity_id) {
			this.infusionPointer = infusionPointer;
			this.entity_id = entity_id;
			this.infusionName = infusionPointerToInfusionName(a, infusionPointer);
		}


	  	private String infusionPointerToInfusionName(AtmelInterpreter a, int infusionPointer) {
	  		// The first element of the infusion pointer is a pointer to the infusion header (in flash)
	  		// The name of the infusion starts at offset 4
	  		int headerPointer = getDataInt16(a, infusionPointer);
	  		int infusionNameAddress = headerPointer + 4;

	        String infusionName = "";
	        int c;
	        do {
	            c=getProgramInt8(a, infusionNameAddress++);
	            if (c != 0) {
	                infusionName += Character.toString((char)c);
	            }
	        } while (c != 0);
	        return infusionName;
	  	}
	}

	private static int dj_di_parentElement_getListSize(AtmelInterpreter a, int listPointer) {
		return a.getProgramByte(listPointer+1) & 0xff;
	}

	private static DjGlobalId dj_vm_getRuntimeClass(AtmelInterpreter a, int vmPointer, int runtime_id) {
		// inline dj_global_id dj_vm_getRuntimeClass(dj_vm *vm, runtime_id_t id) {
		//  dj_global_id ret;
		//  dj_infusion *infusion = vm->infusions;
			int infusionPointer = getDataInt16(a, vmPointer); // infusions is the first field in the vm struct.
		// 	runtime_id_t base = 0;
			int base = 0;

			// System.out.println("vm pointer " + vmPointer + " looking for id " + runtime_id);

		// 	while (infusion!=NULL) {
			while (infusionPointer != 0) {
		// 		base = infusion->class_base;
				base = getDataInt16(a, infusionPointer+10); // ->class_base : skip 10 bytes
		// 		if ((id>=base)&&(id<base + dj_di_parentElement_getListSize(infusion->classList))) {
				int infusionClassListPointer = getDataInt16(a, infusionPointer+2); // ->classList : skip 2 bytes
			// System.out.println("infusion pointer " + infusionPointer + " base " + base);
				if ((runtime_id>=base)&&(runtime_id<base + dj_di_parentElement_getListSize(a, infusionClassListPointer))) {
		// 			ret.infusion = infusion;
		// 			ret.entity_id = id - base;
		// 			return ret;
					return new DjGlobalId(a, infusionPointer, runtime_id - base);
		// 		}
				}
		// 		infusion = infusion->next;
				infusionPointer = getDataInt16(a, infusionPointer+14); // ->next : skip 14 bytes
		// 	}
			}

		//     dj_panic(DJ_PANIC_ILLEGAL_INTERNAL_STATE_NO_RUNTIME_CLASS);
			return null;
		// }
	}

	public static DjGlobalId getGlobalIdFromChunkId(State state, int chunkId) {
        Simulator sim = state.getSimulator();
        AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();

	    final SourceMapping map = sim.getProgram().getSourceMapping();
    	final SourceMapping.Location vmLocation = map.getLocation("vm");
        int vmPointer = getDataInt16(a, vmLocation.vma_addr & 0xffff);
        return dj_vm_getRuntimeClass(a, vmPointer, chunkId);
    }
}


