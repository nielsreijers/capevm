/*
 * Copyright (c) 2009 Cork Institute of Technology, Ireland
 * All rights reserved."
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written
 * agreement is hereby granted, provided that the above copyright notice, the
 * following two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE CORK INSTITUTE OF TECHNOLOGY BE LIABLE TO ANY
 * PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE
 * CORK INSTITUTE OF TECHNOLOGY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * THE CORK INSTITUTE OF TECHNOLOGY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE CORK INSTITUTE OF TECHNOLOGY HAS NO OBLIGATION 
 * TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 */                   

/* AvroraPrint.h
 *
 * This is the C code to print variables to the Avrora emulator
 *
 * How to use:
 *   (1) Include this file "AvroraPrintf.h" in your WSN application 
 *   (2) Send print statements like this:
 *	  
 *	  printChar('a');
 *
 *	  printInt8(44);
 *	  printInt16(3333);
 *	  printInt32(55556666);
 *
 *	  printStr("hello world");
 *
 *	  printHex8(0xFF);
 *    printHex16(0xFFFF);
 *	  printHex32(0xFFFFAAAA);
 *
 *	 (3) Compile and run the code with Avrora including the c-print option.
 *
 * Known bugs/limitations:
 *
 * 	 - If you include many print statements the emulator will slow down 
 * 
 * Notes:	 
 * 	
 * 	 - You can log the print statements to a file including the avrora
 * 	 option printlogfile="logfile.log". The saved file will be in the format
 * 	 logfile.log+nodeid 
 *
 *
 * @author AWS / Rodolfo De Paz http://www.aws.cit.ie/rodolfo
 * @contact avrora@lists.ucla.edu
 */

#ifndef _AVRORATIMER_H_
#define _AVRORATIMER_H_

#include <stdarg.h>
#ifdef __GNUC__
# define AVRORA_TIMER_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_TIMER_INLINE inline
#endif
volatile uint8_t ctimerWatch;

static AVRORA_TIMER_INLINE void avroraStartTimer()
{
	ctimerWatch = 100;
}
static AVRORA_TIMER_INLINE void avroraStopTimer()
{
	ctimerWatch = 101;
}


#endif
