#ifndef _AVRORARTCTRACE_H_
#define _AVRORARTCTRACE_H_

#include <stdarg.h>
#include <avr/pgmspace.h>

#ifdef __GNUC__
# define AVRORA_PRINT_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_PRINT_INLINE inline
#endif
volatile uint8_t rtcMonitorVariable[7];

#define AVRORA_RTC_SINGLEWORDINSTRUCTION 1;
#define AVRORA_RTC_DOUBLEWORDINSTRUCTION 2;
#define AVRORA_RTC_STARTMETHOD           3;
#define AVRORA_RTC_ENDMETHOD             4;
#define AVRORA_RTC_JAVAOPCODE            5;

static AVRORA_PRINT_INLINE void avroraRTCTraceSingleWordInstruction(uint16_t opcode)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = opcode;
	rtcMonitorVariable[0] = AVRORA_RTC_SINGLEWORDINSTRUCTION;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceDoubleWordInstruction(uint16_t opcode1, uint16_t opcode2)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = opcode1;
	*((uint16_t *)(rtcMonitorVariable+3)) = opcode2;
	rtcMonitorVariable[0] = AVRORA_RTC_DOUBLEWORDINSTRUCTION;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceStartMethod(uint8_t method_impl_id, uint32_t address)
{
	rtcMonitorVariable[1] = method_impl_id;
	*((uint32_t *)(rtcMonitorVariable+2)) = address;
	rtcMonitorVariable[0] = AVRORA_RTC_STARTMETHOD;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceEndMethod(uint32_t address, uint16_t jvmmethodsize)
{
	*((uint32_t *)(rtcMonitorVariable+1)) = address;
	*((uint16_t *)(rtcMonitorVariable+5)) = jvmmethodsize;	
	rtcMonitorVariable[0] = AVRORA_RTC_ENDMETHOD;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceDarjeelingOpcodeInProgmem(unsigned int pointer)
{
	rtcMonitorVariable[1] = pgm_read_byte_far(pointer + 0);
	rtcMonitorVariable[2] = pgm_read_byte_far(pointer + 1);
	rtcMonitorVariable[3] = pgm_read_byte_far(pointer + 2);
	rtcMonitorVariable[4] = pgm_read_byte_far(pointer + 3);
	rtcMonitorVariable[5] = pgm_read_byte_far(pointer + 4);
	rtcMonitorVariable[0] = AVRORA_RTC_JAVAOPCODE;
}

#endif
