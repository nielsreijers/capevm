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


#ifndef _AVRORAPROFILER_H_
#define _AVRORAPROFILER_H_

#include <stdarg.h>
#ifdef __GNUC__
# define AVRORA_PROFILER_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_PROFILER_INLINE inline
#endif
volatile uint8_t avrora_profilemonitor;


#define AVRORA_PROFILE_RESET_AND_START  		0x1
#define AVRORA_PROFILE_STOP_COUNTING			0x2


static AVRORA_PROFILER_INLINE void avroraProfilerResetAndStart()
{
	avrora_profilemonitor = AVRORA_PROFILE_RESET_AND_START;
}
static AVRORA_PROFILER_INLINE void avroraProfilerStopCounting()
{
	avrora_profilemonitor = AVRORA_PROFILE_STOP_COUNTING;
}
#endif
