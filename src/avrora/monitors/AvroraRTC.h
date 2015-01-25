#ifndef _AVRORARTCTRACE_H_
#define _AVRORARTCTRACE_H_

#include <stdarg.h>
#ifdef __GNUC__
# define AVRORA_PRINT_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_PRINT_INLINE inline
#endif
volatile uint8_t rtcMonitorVariable[6];

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

static AVRORA_PRINT_INLINE void avroraRTCTraceEndMethod(uint32_t address)
{
	*((uint32_t *)(rtcMonitorVariable+1)) = address;
	rtcMonitorVariable[0] = AVRORA_RTC_ENDMETHOD;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceJavaOpcode(uint8_t opcode)
{
	rtcMonitorVariable[1] = opcode;
	rtcMonitorVariable[0] = AVRORA_RTC_JAVAOPCODE;
}

#endif
