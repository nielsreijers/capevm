#ifndef _AVRORARTCTRACE_H_
#define _AVRORARTCTRACE_H_

#include <stdarg.h>
#ifdef __GNUC__
# define AVRORA_PRINT_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_PRINT_INLINE inline
#endif
volatile uint8_t rtcMonitorVariable[5];

#define AVRORA_RTC_SINGLEWORDINSTRUCTION 1;
#define AVRORA_RTC_DOUBLEWORDINSTRUCTION 2;

static AVRORA_PRINT_INLINE void avroraRTCTraceSingleWordInstruction(uint16_t a)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = a;
	rtcMonitorVariable[0] = AVRORA_RTC_SINGLEWORDINSTRUCTION;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceDoubleWordInstruction(uint16_t a, uint16_t b)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = a;
	*((uint16_t *)(rtcMonitorVariable+3)) = b;
	rtcMonitorVariable[0] = AVRORA_RTC_DOUBLEWORDINSTRUCTION;
}

#endif
