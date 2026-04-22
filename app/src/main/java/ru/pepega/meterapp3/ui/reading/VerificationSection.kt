package ru.pepega.meterapp3.ui.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.VerificationInfo
import ru.pepega.meterapp3.VerificationStatus
import ru.pepega.meterapp3.formatDate
import ru.pepega.meterapp3.formatYears

@Composable
fun MeterVerificationStatusCard(
    verificationDate: Long,
    validityYears: Int,
    verificationInfo: VerificationInfo,
    modifier: Modifier = Modifier
) {
    val verificationStatus = verificationInfo.status
    val isVerificationExpired = verificationStatus is VerificationStatus.EXPIRED

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isVerificationExpired) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = verificationStatus.getIcon(),
                        fontSize = 20.sp
                    )
                    Text(
                        text = stringResource(R.string.meter_verification_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerificationExpired) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Text(
                    text = when (verificationStatus) {
                        is VerificationStatus.EXPIRED -> stringResource(R.string.verification_expired)
                        is VerificationStatus.EXPIRING_SOON -> stringResource(
                            R.string.verification_days_warning,
                            verificationInfo.daysLeft
                        )
                        is VerificationStatus.EXPIRING -> stringResource(
                            R.string.verification_days_expiring,
                            verificationInfo.daysLeft
                        )
                        else -> stringResource(R.string.verification_days_ok, verificationInfo.daysLeft)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = verificationStatus.getColor(),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.verification_date_value,
                            formatDate(verificationDate)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isVerificationExpired) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.82f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                    Text(
                        text = stringResource(R.string.verification_term_value, formatYears(validityYears)),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isVerificationExpired) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.82f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { verificationInfo.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = verificationStatus.getColor(),
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isVerificationExpired) {
                        stringResource(R.string.verification_expired_value, formatDate(verificationInfo.expiryDate))
                    } else {
                        stringResource(R.string.verification_expires_value, formatDate(verificationInfo.expiryDate))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isVerificationExpired) {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
fun MeterVerificationInputLockedCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.76f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.verification_input_locked_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.verification_input_locked_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.88f)
            )
        }
    }
}
