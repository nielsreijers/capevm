package avrora.sim.util;

import avrora.sim.*;
import avrora.arch.AbstractInstr;
import avrora.arch.legacy.LegacyDisassembler;
import cck.text.Terminal;

/**
 * The <code>RTCDisassembler</code> 
 *
 *
 * @author Niels Reijers
 * @see Simulator.Watch
 * @see Counter
 */
public class RTCTrace extends Simulator.Watch.Empty {
	final static LegacyDisassembler disasm = new LegacyDisassembler();

	// public RTCTrace() {
	// 	this.disasm
	// }

    static private int getInt8(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        return l;
    }
    static private int getInt16(AtmelInterpreter a, int offset) {
        final int l = a.getDataByte(offset);
        final int h = a.getDataByte(offset + 1);
        return ((h & 0xff) << 8) + (l & 0xff);
    }

    /**
     * The <code>fireBeforeWrite()</code> method is called before the data address is written by the program.
     *
     * @param state     the state of the simulation
     * @param data_addr the address of the data being referenced
     * @param value     the value being written to the memory location
     */
    public void fireBeforeWrite(State state, int data_addr, byte value) {
		Simulator sim = state.getSimulator();
		AtmelInterpreter a = (AtmelInterpreter) sim.getInterpreter();
        StringBuffer buf = new StringBuffer();

		switch (value) {
			case 1: { // 1 word instruction at data_addr+1:data_addr+2
				byte[] code = new byte[2];
				code[0] = (byte)getInt8(a, data_addr+1);
				code[1] = (byte)getInt8(a, data_addr+2);
				AbstractInstr instr = disasm.disassemble(0, 0, code);
				buf.append("RTCTrace: " + instr + "\n\r");
			}
			break;
			case 2: { // 2 word instruction at data_addr+1:data_addr+2
				byte[] code = new byte[4];
				code[0] = (byte)getInt8(a, data_addr+1);
				code[1] = (byte)getInt8(a, data_addr+2);
				code[2] = (byte)getInt8(a, data_addr+3);
				code[3] = (byte)getInt8(a, data_addr+4);
				AbstractInstr instr = disasm.disassemble(0, 0, code);
				buf.append("RTCTrace: " + instr + "\n\r");
			}
			break;
			default:
				buf.append("RTCTrace: unknown command " + value);
			break;
		}

		synchronized (Terminal.class) {
			Terminal.print(buf.toString());
		}
    }
}
