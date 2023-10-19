#!/bin/bash
sbt it:test
sbt clean coverage compile test coverageReport
