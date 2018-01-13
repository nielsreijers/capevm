#include <stdint.h>
#include <stdbool.h>
#include "config.h"
#include "heap.h"

void __attribute__((noinline)) rtcbenchmark_measure_native_performance(uint16_t NUMNUMBERS, int16_t buffer[], int16_t distance_matrix[], int16_t distance_threshold, bool outliers[]);

void javax_rtcbench_RTCBenchmark_void_test_native() {
	uint16_t NUMNUMBERS = 20;
	int16_t *buffer = dj_mem_checked_alloc(NUMNUMBERS*sizeof(int16_t), CHUNKID_RTCNATIVETESTDATA);
	int16_t *distrance_matrix = dj_mem_checked_alloc(NUMNUMBERS*NUMNUMBERS*sizeof(int16_t), CHUNKID_RTCNATIVETESTDATA);
	bool *outliers = dj_mem_checked_alloc(NUMNUMBERS*sizeof(bool), CHUNKID_RTCNATIVETESTDATA);

	// Fill the array
	for (uint16_t i=0; i<NUMNUMBERS; i++)
		buffer[i] = (NUMNUMBERS - 128 + i); // [-128, 127]
	// Add some outliers
	buffer[2]   = 1000;
	buffer[11]  = -1000;

	rtcbenchmark_measure_native_performance(NUMNUMBERS, buffer, distrance_matrix, 500, outliers);

	for (uint16_t i=0; i<NUMNUMBERS; i++) {
		if (outliers[i] == true) {
			avroraPrintInt16(i);
		}
	}
}